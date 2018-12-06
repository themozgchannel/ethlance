(ns ethlance.server.ipfs
  "Contains all of the necessary functions for adding and retrieving
  IPFS data through the API using core.async.
  
  Also contains the mount component to initialize the IPFS instance."
  (:refer-clojure :exclude [get])
  (:require
   [clojure.core.async :as async :refer [go go-loop <! >! chan close! put! take!] :include-macros true]
   [clojure.tools.reader.edn :as edn]
   [cuerdas.core :as str]
   [clojure.string :refer [ends-with?]]
   [cljs-ipfs-api.core :as ipfs-core]
   [cljs-ipfs-api.files :as ipfs-files]
   [taoensso.timbre :as log]
   [mount.core :as mount :refer [defstate]]
   [district.server.config :refer [config]]))


(def buffer (js/require "buffer"))
(defn to-buffer
  "Convert object into buffer used by IPFS `add!`.

  Notes:

  - Strings can be converted into buffers."
  [x]
  (let [Buffer (.-Buffer buffer)]
    (.from Buffer x)))


(defn start
  "Start the mount component."
  [opts]
  (try
    (let [conn (ipfs-core/init-ipfs opts)]
      (log/info "IPFS Instance Started...")
      conn)
    (catch :default e
      (log/error "Failed to connect to IPFS node")
      (throw (js/Error. "Can't connect to IPFS node")))))


(defstate ipfs
  :start (start (merge (:ipfs @config)
                       (:ipfs (mount/args))))
  :stop (do (log/info "IPFS Instance Stopped...")
            :stopped))


(defn add!
  "Add data to the IPFS network.

  # Return Value

  Returns a pair with the first element being a channel with MAYBE a
  success value, and the second element being a channel with MAYBE an
  error value."
  [^Uint8Array data]
  (let [success-chan (chan 1) error-chan (chan 1)]
    (go
      (ipfs-files/add
       data
       (fn [error result]
         (when error
           (put! error-chan error)
           (close! success-chan))
         (when result
           (put! success-chan (:Hash result))
           (close! error-chan)))))
    [success-chan error-chan]))


(defn get
  [ipfs-hash]
  (let [success-chan (chan 1) error-chan (chan 1)]
    (go
      (ipfs-files/fget
       (str "/ipfs/" ipfs-hash)
       {:req-opts {:compress false :json true}}
       (fn [error result]
         (when error
           (put! error-chan error)
           (close! success-chan))
         (when result
           (put! success-chan result)
           (close! error-chan)))))
    [success-chan error-chan]))


(defn add-edn!
  "Add a clojure data structure to the IPFS network, where `data` is a
  clojure data structure.

  # Return Value

  Returns a pair with the first element being a channel with MAYBE a
  success value, and the second element being a channel with MAYBE an
  error value."
  [data]
  (add! (to-buffer (pr-str data))))


(defn parse-edn-result
  "Parses EDN from the metadata in the IPFS result string.

  Notes:

  - The result from the IPFS-API contains additional CORS metadata
  that needs to be parsed out before it can be parsed into an EDN
  value.
  - Can only parse edn maps.
  "
  [s]
  (try
    (-> (re-find #".*?(\{.*\})" s) second edn/read-string)
    (catch js/Error e
      (log/error (str "EDN Parse Error: " e))
      nil)))


(defn get-edn
  "Get the clojure data structure stored as an EDN value for the
  resulting `hash`."
  [hash]
  (let [success-chan (chan 1) error-chan (chan 1)
        [result-channel err-c] (get hash)]
    (go
      (when-let [err (<! err-c)]
        (>! error-chan err)
        (close! success-chan))
      
      (when-let [result (<! result-channel)]
        (if-let [parsed-result (parse-edn-result result)]
          (do (>! success-chan parsed-result)
              (close! error-chan))
          (do (>! error-chan (ex-info "Failed to parse edn value" {:unparsed-result (pr-str result)}))
              (close! success-chan)))))
    [success-chan error-chan]))

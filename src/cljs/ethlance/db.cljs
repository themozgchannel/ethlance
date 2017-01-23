(ns ethlance.db
  (:require [cljs-web3.core :as web3]
            [ethlance.utils :as u]
            [re-frame.core :refer [dispatch]]
            [cljs-time.core :as t]
            [ethlance.constants :as constants]))


(def default-db
  {:testrpc? false #_ true
   :web3 nil
   :active-page (u/match-current-location)
   :provides-web3? false
   :contracts-not-found? false
   :window/width-size (u/get-window-width-size js/window.innerWidth)
   :drawer-open? false
   :search-freelancers-filter-open? false
   :search-jobs-filter-open? false
   :selected-currency :eth
   :snackbar {:open? false
              :message ""
              :auto-hide-duration 5000
              :on-request-close #(dispatch [:snackbar/close])}
   :eth/config {:max-user-languages 10
                :min-user-languages 1
                :max-freelancer-categories (dec (count constants/categories))
                :min-freelancer-categories 1
                :max-freelancer-skills 10
                :min-freelancer-skills 1
                :max-job-skills 7
                :min-job-skills 1
                :max-user-description 1000
                :max-job-description 1000
                :min-job-description 100
                :max-invoice-description 500
                :max-feedback 1000
                :min-feedback 50
                :max-job-title 100
                :min-job-title 10
                :max-user-name 40
                :min-user-name 5
                :max-freelancer-job-title 50
                :min-freelancer-job-title 4
                :max-contract-desc 500
                :max-proposal-desc 500
                :max-invitation-desc 500
                :max-skills-create-at-once 50 #_10
                :adding-skills-enabled? 1}
   :eth/contracts {:ethlance-user {:name "EthlanceUser" :setter? true :address "0xb0f1102af4f36290ec7db1461ab23d5a55460715"}
                   :ethlance-job {:name "EthlanceJob" :setter? true :address "0x2128629f1546072a0a833041fe4445584d792792"}
                   :ethlance-contract {:name "EthlanceContract" :setter? true  :address "0xa5d81ebae0dfe33a20a52b6cc76cebea6530e2c5"}
                   :ethlance-invoice {:name "EthlanceInvoice" :setter? true :address "0x348585f2c1f08abd701846df04ef737aaa2979d5"}
                   :ethlance-config {:name "EthlanceConfig" :setter? true :address "0x410f475553f7e1f701d503fc24fc48822fd1ccb4"}
                   :ethlance-db {:name "EthlanceDB" :address "0xf3e6364666138d997caf832a7bb0688316ac1e5f"}
                   :ethlance-views {:name "EthlanceViews" :address "0xa2faa7a777d3efc20453dc073b8d8009db6594a6"}
                   :ethlance-search {:name "EthlanceSearch" :address "0xac9a6b36d5cbc64238dd34b390e3073c7f30cbeb"}}
   :my-addresses []
   :active-address nil
   :my-users-loaded? false
   :blockchain/addresses {}
   :blockchain/connection-error? false
   :conversion-rates {}
   :app/users {}
   :app/jobs {}
   :app/contracts {}
   :app/invoices {}
   :app/skills {}
   :app/skill-count 0
   :skill-load-limit 5

   :list/my-users {:items [] :loading? true :params {}}
   :list/contract-invoices {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/job-proposals {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :asc}
   :list/job-feedbacks {:items [] :loading? true :params {} :offset 0 :initial-limit 1 :limit 1 :show-more-limit 2 :sort-dir :desc}
   :list/job-invoices {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/employer-invoices-pending {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/employer-invoices-paid {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/freelancer-invoices-pending {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/freelancer-invoices-paid {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/search-freelancers {:items [] :loading? true :params {} :offset 0 :limit 3}
   :list/search-jobs {:items [] :loading? true :params {} :offset 0 :limit 10}
   :list/freelancer-feedbacks {:items [] :loading? true :params {} :offset 0 :initial-limit 1 :limit 1 :show-more-limit 2 :sort-dir :desc}
   :list/employer-feedbacks {:items [] :loading? true :params {} :offset 0 :initial-limit 1 :limit 1 :show-more-limit 2 :sort-dir :desc}
   :list/freelancer-invitations {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/freelancer-proposals {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/freelancer-contracts {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/freelancer-contracts-open {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/freelancer-contracts-done {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/employer-jobs-open {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/employer-jobs-done {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/employer-jobs {:items [] :loading? true :params {} :offset 0 :limit 4 :sort-dir :desc}
   :list/freelancer-my-open-contracts {:items [] :loading? true :params {}}
   :list/employer-jobs-open-select-field {:items [] :loading? false :params {}}

   :form.invoice/pay-invoice {:loading? false :gas-limit 200000}
   :form.invoice/cancel-invoice {:loading? false :gas-limit 200000}
   :form.job/set-hiring-done {:loading? false :gas-limit 200000}
   :form.job/add-job {:loading? false
                      :gas-limit 2000000
                      :data {:job/title ""
                             :job/description ""
                             :job/skills []
                             :job/language 40
                             :job/budget 0
                             :job/category 0
                             :job/payment-type 1
                             :job/experience-level 1
                             :job/estimated-duration 1
                             :job/hours-per-week 1
                             :job/freelancers-needed 1}
                      :errors #{:job/title :job/description :job/skills :job/category}}
   :form.contract/add-invitation {:loading? false
                                  :gas-limit 700000
                                  :data {:invitation/description ""
                                         :contract/job 0}
                                  :errors #{:contract/job}}

   :form.contract/add-proposal {:loading? false
                                :gas-limit 700000
                                :data {:proposal/description ""
                                       :proposal/rate 0}
                                :errors #{:proposal/description}}

   :form.contract/add-contract {:loading? false
                                :gas-limit 700000
                                :data {:contract/description ""
                                       :contract/hiring-done? false}
                                :errors #{}}

   :form.contract/add-feedback {:loading? false
                                :gas-limit 700000
                                :data {:contract/feedback ""
                                       :contract/feedback-rating 0}
                                :errors #{:contract/feedback}}

   :form.invoice/add-invoice {:loading? false
                              :gas-limit 700000
                              :data {:invoice/contract nil
                                     :invoice/description ""
                                     :invoice/amount 0
                                     :invoice/worked-hours 0
                                     :invoice/worked-from (u/timestamp-js->sol (u/get-time (u/week-ago)))
                                     :invoice/worked-to (u/timestamp-js->sol (u/get-time (t/today-at-midnight)))}
                              :errors #{:invoice/contract}}

   :form.config/add-skills {:loading? false
                            :gas-limit 4500000
                            :data {:skill/names []}
                            :errors #{:skill/names}}

   :form.user/set-user {:loading? false
                        :gas-limit 500000
                        :data {}
                        :errors #{}}

   :form.user/set-freelancer {:loading? false
                              :gas-limit 1000000
                              :data {}
                              :errors #{}
                              :open? false}

   :form.user/set-employer {:loading? false
                            :gas-limit 1000000
                            :data {}
                            :errors #{}
                            :open? false}

   :form.user/register-freelancer {:loading? false
                                   :gas-limit 2000000
                                   :open? true
                                   :data {:user/name ""
                                          :user/email ""
                                          :user/gravatar ""
                                          :user/country 0
                                          :user/languages [40]
                                          :freelancer/available? true
                                          :freelancer/job-title ""
                                          :freelancer/hourly-rate 1
                                          :freelancer/categories []
                                          :freelancer/skills []
                                          :freelancer/description ""}
                                   :errors #{:user/name :user/country :freelancer/job-title
                                             :freelancer/categories :freelancer/skills}}

   :form.user/register-employer {:loading? false
                                 :gas-limit 2000000
                                 :open? true
                                 :data {:user/name ""
                                        :user/email ""
                                        :user/gravatar ""
                                        :user/country 0
                                        :user/languages [40]
                                        :employer/description ""}
                                 :errors #{:user/name :user/country}}


   :form/search-jobs {:search/category 0
                      :search/skills []
                      :search/payment-types [1 2]
                      :search/experience-levels [1 2 3]
                      :search/estimated-durations [1 2 3 4]
                      :search/hours-per-weeks [1 2]
                      :search/min-budget 0
                      :search/min-employer-avg-rating 0
                      :search/min-employer-ratings-count 0
                      :search/country 0
                      :search/state 0
                      :search/language 0
                      :search/offset 0
                      :search/limit 10}

   :form/search-freelancers {:search/category 0
                             :search/skills []
                             :search/min-avg-rating 0
                             :search/min-freelancer-ratings-count 0
                             :search/min-hourly-rate 0
                             :search/max-hourly-rate 0
                             :search/country 0
                             :search/state 0
                             :search/language 0
                             :search/offset 0
                             :search/limit 3}
   }
  )

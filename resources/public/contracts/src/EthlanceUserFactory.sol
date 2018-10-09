pragma solidity ^0.4.24;

import "./EthlanceRegistry.sol";
import "./EthlanceUser.sol";
import "proxy/MutableForwarder.sol";
import "proxy/Forwarder.sol";

/// @title Ethlance User Factory
/// @dev Used for the creation of users, along with the relation to
/// Candidates, Employers and Arbiters.
contract EthlanceUserFactory {
    uint public constant version = 1;
    EthlanceRegistry public constant registry = EthlanceRegistry(0xdaBBdABbDABbDabbDaBbDabbDaBbdaBbdaBbDAbB);

    //
    // Methods
    //

    /// @dev Fire events specific to the UserFactory
    /// @param event_name Unique to give the fired event
    /// @param event_data Additional event data to include in the
    /// fired event.
    function fireEvent(string event_name, uint[] event_data) private {
	registry.fireEvent(event_name, version, event_data);
    }


    /// @dev Create User for the given address
    /// @param _address Address to the create the user for.
    /// @param _metahash IPFS metahash.
    function createUser(address _address, string _metahash)
	// FIXME: isAuthorized
	public
	isRegisteredUser(_address)
	returns (uint) {

	address user_fwd = new Forwarder(); // Proxy Contract with
					    // target(EthlanceUser)
	EthlanceUser user = EthlanceUser(address(user_fwd));
	user.construct(registry, _address, _metahash);

	uint user_id = registry.pushUser(_address, address(user));

	uint[] memory edata = new uint[](1);
	edata[0] = user_id;
	fireEvent("UserFactoryCreatedUser", edata);

	return user_id;
    }


    //
    // Views
    //


    /// @dev Returns IPFS metahash for the given `user_id`
    /// @param user_id User Id for the given user
    /// @return The IPFS metahash for the given user
    function getUserByID(uint user_id)
	public view returns(EthlanceUser) {
	require(user_id <= registry.getUserCount(),
		"Given user_id is out of bounds.");
	
	// Note: user_id is +1 of the index.
	EthlanceUser user = EthlanceUser(registry.getUserByIndex(user_id-1));

	return user;
    }


    /// @dev Returns IPFS metahash for the given address
    /// @param _address The address of the user.
    /// @return The IPFS metahash for the given user.
    function getUserByAddress(address _address)
	public view
	isRegisteredUser(_address)
	returns(EthlanceUser)
    {
	EthlanceUser user = EthlanceUser(registry.getUserByAddress(_address));

	return user;
    }


    /// @dev Returns the current User Contract Address
    /// @return The current user contract address.
    function getCurrentUser()
	public view
	isRegisteredUser(msg.sender)
	returns (EthlanceUser)
    {
	EthlanceUser user = EthlanceUser(registry.getUserByAddress(msg.sender));
	
	return user;
    }


    /// @dev Returns the number of users.
    /// @return The number of users.
    function getUserCount()
	public view returns (uint) {

	return registry.getUserCount();
    }


    //
    // Modifiers
    //


    /// @dev Checks if the given address is a registered User.
    modifier isRegisteredUser(address _address) {
	require(registry.getUserByAddress(_address) != 0x0,
		"Given address identity is not a registered User.");
	_;
    }
}
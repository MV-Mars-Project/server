package be.howest.ti.mars.logic.controller;

import be.howest.ti.mars.logic.controller.security.UserToken;
import be.howest.ti.mars.logic.data.MarsRepository;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UserAccount extends BaseAccount {
    private List<UserAccount> friends;
    MarsRepository repo = new MarsRepository();

    public UserAccount( String username, String password, int homeAddressEndpoint, String address) {
        super(homeAddressEndpoint, password, username, address);
    }

    public UserAccount(String username, String password, int homeAddressEndpoint, String address, UserToken userToken) {
        super(userToken, homeAddressEndpoint, password, username, address);
        friends = new LinkedList<>();
    }

    public List<UserAccount> getFriends(){
        return friends;
    }

    public UserAccount addFriend(UserAccount friend){
        friends.add(friend);
        repo.beFriend(getUsername(), friend.getUsername());
        return friend;
    }

    public UserAccount removeFriend(UserAccount friend){
        friends.remove(friend);
        repo.removeFriend(getUsername(), friend.getUsername());
        return friend;
    }
}

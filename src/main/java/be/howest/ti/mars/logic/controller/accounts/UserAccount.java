package be.howest.ti.mars.logic.controller.accounts;

import be.howest.ti.mars.logic.controller.enums.NotificationType;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class UserAccount extends BaseAccount {
    private boolean sharesLocation;
    private String displayName;
    private static final String CHNL_TO_CLIENT_NOTIFICATION = "events.client.";

    public UserAccount(String username, String password, int homeAddressEndpoint, String address) {
        super(username, password, address, homeAddressEndpoint);
        sharesLocation = false;
        displayName = username;
        subscriptionId = 0;
    }

    public UserAccount(String username, String password, String displayName, int homeAddressEndpoint, String address, boolean sharesLocation) {
        super(null, homeAddressEndpoint, password, username, address);
        this.sharesLocation = sharesLocation;
        this.displayName = displayName;
    }

    public UserAccount(String name) {
        super(name);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setSharesLocation(boolean sharesLocation) {
        repo.setShareLocation(this, sharesLocation);
        this.sharesLocation = sharesLocation;
    }

    public int getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(int subscriptionId) {
        repo.setUserSubscription(this, subscriptionId);
        this.subscriptionId = subscriptionId;
    }

    public void setDisplayName(String displayName) {
        repo.setDisplayName(this, displayName);
        this.displayName = displayName;
    }

    public void addFriend(String friendName) {
        repo.beFriend(getUsername(), friendName);
    }

    public void removeFriend(String friendName) {
        repo.removeFriend(getUsername(), friendName);
    }

    public boolean isSharesLocation() {
        return sharesLocation;
    }

    public void sendNotification(Vertx vertx, NotificationType type, int id) {
        if (accountToken != null) {
            JsonObject message = new JsonObject();
            message.put("id", id);
            message.put("type", type);
            vertx.eventBus().send(CHNL_TO_CLIENT_NOTIFICATION + accountToken.getTokenBase64(), message);
        }
    }
}

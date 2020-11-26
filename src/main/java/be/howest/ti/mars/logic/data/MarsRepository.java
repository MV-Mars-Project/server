package be.howest.ti.mars.logic.data;

import be.howest.ti.mars.logic.controller.Delivery;
import be.howest.ti.mars.logic.controller.Endpoint;
import be.howest.ti.mars.logic.controller.Travel;
import be.howest.ti.mars.logic.controller.accounts.BaseAccount;
import be.howest.ti.mars.logic.controller.accounts.BusinessAccount;
import be.howest.ti.mars.logic.controller.accounts.UserAccount;
import be.howest.ti.mars.logic.controller.converters.ShortEndpoint;
import be.howest.ti.mars.logic.controller.subscription.BusinessSubscription;
import be.howest.ti.mars.logic.controller.subscription.BusinessSubscriptionInfo;
import be.howest.ti.mars.logic.controller.subscription.UserSubscription;

import java.util.List;
import java.util.Set;

public interface MarsRepository {
    // accounts
    void addAccount(BaseAccount account);
    Set<BaseAccount> getAccounts();
    Set<UserAccount> getUserAccounts();
    Set<BusinessAccount> getBusinessAccounts();

    // Endpoint
    Set<ShortEndpoint> getEndpoints();

    void addEndpoint(String endpoint);

    Endpoint getEndpoint(int id);

    // Favorite
    Set<ShortEndpoint> getFavoriteEndpoints(BaseAccount acc);

    void favoriteEndpoint(BaseAccount acc, int id);

    void unFavoriteEndpoint(BaseAccount user, int id);

    // User
    void addUser(UserAccount user);

    void changePassword(BaseAccount acc, String newPW);

    void setShareLocation(UserAccount user, boolean shareLocation);

    void setDisplayName(UserAccount acc, String displayName);

    // Friends
    Set<UserAccount> getFriends(UserAccount user, Set<UserAccount> users);

    void beFriend(String name, String friendName);

    void removeFriend(String name, String friendName);

    // business
    void addBusiness(BusinessAccount business);
    void resetPods(BusinessAccount acc);

    // Travel
    List<Travel> getTravelHistory(UserAccount acc);

    int travel(UserAccount user, Travel travel);

    void cancelTravel(UserAccount user, int tripID);

    // Deliveries
    List<Delivery> getDeliveries(BusinessAccount acc); // TODO: 21-11-2020 also not available in spec

    int addDelivery(Delivery delivery);

    Object getDeliveryInformation(BaseAccount acc, int id);

    // Subscriptions
    Set<UserSubscription> getUserSubscriptions(); // TODO: 21-11-2020 add to spec and webserver

    Set<BusinessSubscription> getBusinessSubscriptions(); // TODO: 21-11-2020 add to spec and webserver

    UserSubscription getUserSubscription(UserAccount user);

    BusinessSubscription getBusinessSubscription(BusinessAccount business);

    BusinessSubscriptionInfo getBusinessSubscriptionInfo(BusinessAccount business); // this returns the used amount of pods by that business that day

    void updateBusinessSubscription(boolean b, BusinessAccount acc);

    void setUserSubscription(UserAccount user, int subscriptionId);

    void setBusinessSubscription(BusinessAccount business, int subscriptionId);

    // Report
    Set<String> getReportSections();

    void addReport(BaseAccount baseAccount, String section, String body);
}

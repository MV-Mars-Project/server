package be.howest.ti.mars.webserver;

import be.howest.ti.mars.logic.controller.MTTSController;
import be.howest.ti.mars.logic.controller.accounts.BaseAccount;
import be.howest.ti.mars.logic.controller.accounts.BusinessAccount;
import be.howest.ti.mars.logic.controller.accounts.UserAccount;
import be.howest.ti.mars.logic.controller.enums.DeliveryType;
import be.howest.ti.mars.logic.controller.enums.NotificationType;
import be.howest.ti.mars.logic.controller.security.AccountToken;
import be.howest.ti.mars.logic.controller.security.SecureHash;
import be.howest.ti.mars.logic.data.Repositories;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

class MarsOpenApiBridge {
    public static final String AUTHORIZATION_TOKEN_PREFIX = "Bearer ";
    private static final String TOKEN = "token";
    private static final Random rand = new Random();
    private static final Timer timer = new Timer();
    private static final long RESET_PERIOD = 1000L * 60L * 60L * 24L;
    private static Vertx vertx;
    private final MTTSController controller;



    MarsOpenApiBridge() {
        this.controller = new MTTSController();
    }

    public static void setVertx(Vertx vertx) {
        MarsOpenApiBridge.vertx = vertx;
    }

    private static TimerTask wrap(Runnable r) { // wish this could be cleaner but it isn't a functional interface
        return new TimerTask() {
            @Override
            public void run() {
                r.run();
            }
        };
    }

    public Object getMessage(RoutingContext ctx) {
        return controller.getMessage();
    }

    public Object createAccount(RoutingContext ctx) {
        JsonObject json = ctx.getBodyAsJson();
        controller.createAccount(
                json.getString("name"),
                SecureHash.getHashEncoded(json.getString("password")),
                json.getString("homeAddress"),
                json.getInteger("homeEndpointId"),
                json.getBoolean("businessAccount")
        );

        return "Successfully created an account";
    }

    private Long getETA() { // randomized delay
        return rand.nextInt(5) * 1000L;
    }

    public Object sendPackage(RoutingContext ctx) { // TODO: 21-11-2020 add missing validation: only business can send large packages, etc , from != dest
        JsonObject json = ctx.getBodyAsJson();
        boolean isUser = isUserAccountToken(ctx);
        int id = controller.sendPackage(DeliveryType.enumOf(json.getString("deliveryType")),
                json.getInteger("from"),
                json.getInteger("destination"),
                getAccount(ctx),
                isUser
        );
        if (isUser) timer.schedule(wrap(() -> getUserAccount(ctx).sendNotification(vertx, NotificationType.PACKAGE, id)), getETA());
        JsonObject delivery = new JsonObject();
        delivery.put("deliveryId", id);
        return delivery;
    }

    public Object login(RoutingContext ctx) {
        JsonObject json = ctx.getBodyAsJson();
        return controller.login(json.getString("name"), SecureHash.getHashEncoded(json.getString("password")));
    }

    public Object logout(RoutingContext ctx) {
        controller.logout(getAccount(ctx));
        return "Bye bye";
    }

    public Object changePassword(RoutingContext ctx) {
        controller.changePassword(getAccount(ctx), SecureHash.getHashEncoded(ctx.getBodyAsJson().getString("newPassword")));
        return "Successfully changed your password.";
    }

    public Object getAccountInformation(RoutingContext ctx) {
        if (isUserAccountToken(ctx)) {
            return controller.getUserAccountInformation(getUserAccount(ctx));
        } else {
            return controller.getBusinessAccountInformation(getBusinessAccount(ctx));
        }
    }

    public Object viewFriends(RoutingContext ctx) {
        return Repositories.getFriendsRepo().getFriends(getUserAccount(ctx))
                .stream()
                .map(UserAccount::getUsername)
                .collect(Collectors.toUnmodifiableList());
    }

    public Object addFriend(RoutingContext ctx) { // TODO: 21-11-2020 cant friend businesses
        UserAccount user = getUserAccount(ctx);
        String friendName = ctx.request().getParam("fName");
        return controller.addFriend(user, friendName);
    }

    public Object removeFriend(RoutingContext ctx) {
        UserAccount user = getUserAccount(ctx);
        String friendName = ctx.request().getParam("fName");
        return controller.removeFriend(user, friendName);
    }

    public Object viewSubscriptions(RoutingContext ctx) {
        if (isUserAccountToken(ctx)) {
            return Repositories.getSubscriptionRepo().getUserSubscriptions();
        } else {
            return Repositories.getSubscriptionRepo().getBusinessSubscriptions();
        }
    }

    public Object buySubscription(RoutingContext ctx) {
        int subscriptionId = ctx.getBodyAsJson().getInteger("subscriptionId");
        if (isUserAccountToken(ctx)) {
            getUserAccount(ctx).setSubscriptionId(subscriptionId);
        } else {
            getBusinessAccount(ctx).setSubscriptionId(subscriptionId);
        }
        return "Thank you for buying a subscription.";
    }

    public Object stopSubscription(RoutingContext ctx) {

        if (isUserAccountToken(ctx)) {
            getUserAccount(ctx).setSubscriptionId(0);
        } else {
            getBusinessAccount(ctx).setSubscriptionId(0);
        }
        return "We are sorry that you have discontinued your current subscription.";
    }

    public Object viewSubscriptionInfo(RoutingContext ctx) {
        return Repositories.getSubscriptionRepo().getBusinessSubscriptionInfo(getBusinessAccount(ctx));
    }

    public Object shareLocation(RoutingContext ctx) {
        getUserAccount(ctx).setSharesLocation(true);
        return "Now sharing location with friends.";
    }

    public Object stopSharingLocation(RoutingContext ctx) { // TODO: 21-11-2020 should we care if someone tries stopping his location sharing when it is already stopped ?
        getUserAccount(ctx).setSharesLocation(false);
        return "Not sharing location anymore with friends.";
    }

    public Object getEndpoints(RoutingContext ctx) {
        return Repositories.getEndpointsRepo().getEndpoints();
    }

    public Object getEndpoint(RoutingContext ctx) {
        return Repositories.getEndpointsRepo().getEndpoint(Integer.parseInt(ctx.request().getParam("id")));
    }

    public Object favoriteEndpoint(RoutingContext ctx) {
        int endpointId = Integer.parseInt(ctx.request().getParam("id"));
        controller.favoriteEndpoint(getAccount(ctx), endpointId);
        return "Successfully favored this endpoint.";
    }

    public Object unFavoriteEndpoint(RoutingContext ctx) {
        int endpointId = Integer.parseInt(ctx.request().getParam("id"));
        controller.unFavoriteEndpoint(getAccount(ctx), endpointId);
        return "Successfully unfavored this endpoint.";
    }

    public Object addReport(RoutingContext ctx) {
        JsonObject json = ctx.getBodyAsJson();
        Repositories.getReportsRepo().addReport(
                getAccount(ctx),
                json.getString("section"),
                json.getString("description")
        );
        return "Report has been received.";
    }

    public Object getReportSections(RoutingContext ctx) {
        return Repositories.getReportsRepo().getReportSections();
    }

    public Object travel(RoutingContext ctx) {
        int from = ctx.getBodyAsJson().getInteger("from");
        int destination = ctx.getBodyAsJson().getInteger("destination");
        String podType = ctx.getBodyAsJson().getString("podType");
        UserAccount user = getUserAccount(ctx);
        int id = controller.travel(user, from, destination, podType);
        timer.schedule(wrap(() -> user.sendNotification(vertx, NotificationType.TRAVEL, id)), getETA());

        JsonObject travel = new JsonObject();
        travel.put("travelId", id);
        return travel;
    }

    public Object getTravelHistory(RoutingContext ctx) {
        return controller.getTravelHistory(getUserAccount(ctx));
    }

    public Object cancelTrip(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.request().getParam("id"));
        controller.cancelTrip(getUserAccount(ctx), id);

        return null;
    }

    public Object setDisplayName(RoutingContext ctx) {
        String newDN = ctx.getBodyAsJson().getString("newDisplayName");
        getUserAccount(ctx).setDisplayName(newDN);
        return "Successfully changed your display name to " + newDN;
    }

    public Object getCurrentRouteInfo(RoutingContext ctx) {
        return controller.getCurrentRouteInfo(getUserAccount(ctx));
    }

    public Object getDeliveries(RoutingContext ctx) {
        return controller.getDeliveries(getBusinessAccount(ctx));
    }

    public Object getDeliveryInformation(RoutingContext ctx) {
        return controller.getDelivery(getAccount(ctx), Integer.parseInt(ctx.request().getParam("id")));
    }

    //------------------------------------------------------------------------------------------------------------------

    public boolean isUserAccountToken(RoutingContext ctx) {
        return getUserAccount(ctx) != null;
    }

    public boolean verifyBusinessAccountToken(RoutingContext ctx) {
        return getBusinessAccount(ctx) != null;
    }

    private BaseAccount getAccount(RoutingContext ctx) {
        UserAccount account = getUserAccount(ctx);
        return account != null ? account : getBusinessAccount(ctx);
    }

    private UserAccount getUserAccount(RoutingContext ctx) {
        AccountToken accountToken = Json.decodeValue(new JsonObject().put(TOKEN, getBearerToken(ctx)).toString(), AccountToken.class);
        return controller.getUserAccounts().stream()
                .filter(acc -> accountToken.equals(acc.getAccountToken()))
                .findAny()
                .orElse(null);
    }

    private BusinessAccount getBusinessAccount(RoutingContext ctx) {
        AccountToken accountToken = Json.decodeValue(new JsonObject().put(TOKEN, getBearerToken(ctx)).toString(), AccountToken.class);
        return controller.getBusinessAccounts().stream()
                .filter(acc -> accountToken.equals(acc.getAccountToken()))
                .findAny()
                .orElse(null);
    }

    public String getBearerToken(RoutingContext ctx) {
        String header = ctx.request().getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(AUTHORIZATION_TOKEN_PREFIX)) {
            return null;
        } else {
            return header.substring(AUTHORIZATION_TOKEN_PREFIX.length());
        }
    }

    private void resetBusinessUsedPods() {
        controller.getBusinessAccounts().forEach(acc -> Repositories.getSubscriptionRepo().resetPods(acc));
    }

    public void startDailyResetCompanyPods() {
        timer.scheduleAtFixedRate(wrap(this::resetBusinessUsedPods), 0, RESET_PERIOD);
    }


}

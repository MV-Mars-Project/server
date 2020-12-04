package be.howest.ti.mars.logic.controller.repoTests;

import be.howest.ti.mars.logic.controller.MTTSController;
import be.howest.ti.mars.logic.controller.accounts.BusinessAccount;
import be.howest.ti.mars.logic.controller.accounts.UserAccount;
import be.howest.ti.mars.logic.controller.exceptions.DatabaseException;
import be.howest.ti.mars.logic.controller.subscription.BusinessSubscription;
import be.howest.ti.mars.logic.controller.subscription.UserSubscription;
import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.data.repositories.AccountsRepository;
import be.howest.ti.mars.logic.data.util.MarsConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SubscriptionRepoTest {
    private static final AccountsRepository accountRepo = Repositories.getAccountsRepo();
    private static final MTTSController controller = new MTTSController();
    private static final UserAccount testDanny = new UserAccount("Danny", "Danny", 5, "MarsStreet 69");
    private static final UserAccount testDebby = new UserAccount("Debby", "Debby", 3, "WestStreet 420");
    private static final BusinessAccount testPol = new BusinessAccount("Pol", "Pol", 6, "Earthstreet 23");

    @BeforeAll
    static void start() {

        try {
            MarsConnection.configure("jdbc:h2:~/mars-db", "", "", 9000);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        accountRepo.addUser(testDanny);
        accountRepo.addUser(testDebby);
        accountRepo.addBusiness(testPol);
    }

    @AfterAll
    static void end() {
        MarsConnection.getInstance().cleanUp();
    }

    @Test
    void getUserSubscriptions(){
        assertEquals(4, Repositories.getSubscriptionRepo().getUserSubscriptions().size());
    }

    @Test
    void getBusinessSubscriptions(){
        assertEquals(5, Repositories.getSubscriptionRepo().getBusinessSubscriptions().size());
    }

    @Test
    void setUserSub(){
        Repositories.getSubscriptionRepo().setUserSubscription(testDanny, 1);
        assertEquals(1, Repositories.getSubscriptionRepo().getUserSubscription(testDanny).getId());

        assertThrows(DatabaseException.class, ()-> Repositories.getSubscriptionRepo().setUserSubscription(testDebby, 6));
    }

    @Test
    void setBusinessSub(){
        Repositories.getSubscriptionRepo().setBusinessSubscription(testPol, 1);
        assertEquals(1, Repositories.getSubscriptionRepo().getBusinessSubscription(testPol).getId());

        assertThrows(DatabaseException.class, ()-> Repositories.getSubscriptionRepo().setBusinessSubscription(testPol, -1));
    }

    @Test
    void getUserSub(){
        List<UserSubscription> subscriptions = new LinkedList<>(Repositories.getSubscriptionRepo().getUserSubscriptions());
        String firstSubName = subscriptions.get(1).getName();
        int firstSubId = subscriptions.get(1).getId();

        assertEquals(firstSubName, Repositories.getSubscriptionRepo().getUserSubscription(testDanny).getName());
        assertEquals(firstSubId, Repositories.getSubscriptionRepo().getUserSubscription(testDanny).getId());
    }

    @Test
    void getBusinessSub(){
        List<BusinessSubscription> subscriptions = new LinkedList<>(Repositories.getSubscriptionRepo().getBusinessSubscriptions());
        String firstSubName = subscriptions.get(1).getName();
        int firstSubId = subscriptions.get(1).getId();

        assertEquals(firstSubName, Repositories.getSubscriptionRepo().getBusinessSubscription(testPol).getName());
        assertEquals(firstSubId, Repositories.getSubscriptionRepo().getBusinessSubscription(testPol).getId());
    }

}
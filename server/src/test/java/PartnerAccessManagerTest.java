import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartnerAccessManagerTest {

    @Test
    void requestAccessCodeGeneratesCodeAndMaskedDestination() {
        PartnerAccessManager manager = new PartnerAccessManager(Map.of(
                "Pizza Palace", "ops@pizzapalace.foodie-demo.com"
        ));

        PartnerAccessManager.AccessCodeIssueResult result = manager.requestAccessCode("Pizza Palace");

        assertTrue(result.success);
        assertEquals("op***@pizzapalace.foodie-demo.com", result.maskedDestination);
        assertNotNull(result.accessCode);
        assertEquals(6, result.accessCode.length());
        assertEquals(PartnerAccessManager.LoginValidationResult.SUCCESS,
                manager.validateLogin("Pizza Palace", result.accessCode));
    }

    @Test
    void loginFailsWhenCodeIsMissingOrWrong() {
        PartnerAccessManager manager = new PartnerAccessManager(Map.of(
                "Pizza Palace", "ops@pizzapalace.foodie-demo.com"
        ));

        assertEquals(PartnerAccessManager.LoginValidationResult.NO_ACTIVE_CODE,
                manager.validateLogin("Pizza Palace", "123456"));

        PartnerAccessManager.AccessCodeIssueResult result = manager.requestAccessCode("Pizza Palace");

        assertEquals(PartnerAccessManager.LoginValidationResult.INVALID_CODE,
                manager.validateLogin("Pizza Palace", "000000"));
        assertEquals(PartnerAccessManager.LoginValidationResult.SUCCESS,
                manager.validateLogin("Pizza Palace", result.accessCode));
    }

    @Test
    void storeNotFoundIsReportedConsistently() {
        PartnerAccessManager manager = new PartnerAccessManager(Map.of(
                "Pizza Palace", "ops@pizzapalace.foodie-demo.com"
        ));

        assertTrue(!manager.requestAccessCode("Unknown Store").success);
        assertEquals(PartnerAccessManager.LoginValidationResult.STORE_NOT_FOUND,
                manager.validateLogin("Unknown Store", "123456"));
        assertEquals("op***@pizzapalace.foodie-demo.com", manager.getMaskedDestination("Pizza Palace"));
    }
}
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerCommandProcessorTest {

    @Test
    void rejectsEmptyAndMalformedCommands() {
        ServerCommandProcessor processor = createProcessor();

        assertEquals("ERROR: Empty command", processor.processCommand("   "));
        assertEquals("ERROR: Store name is required", processor.processCommand("REQUEST_PARTNER_ACCESS_CODE:   "));
        assertEquals("ERROR: Access code is required", processor.processCommand("PARTNER_LOGIN:Pizza Palace:   "));
        assertEquals("ERROR: Product name is required", processor.processCommand("BUY:Burger Barn::1"));
    }

    @Test
    void rejectsInvalidPartnerCodeFormatsAndRepeatedRequests() {
        ServerCommandProcessor processor = createProcessor();

        assertTrue(processor.processCommand("REQUEST_PARTNER_ACCESS_CODE:Pizza Palace").startsWith("CODE_SENT:"));
        assertEquals("ERROR: Access code must be 6 digits", processor.processCommand("PARTNER_LOGIN:Pizza Palace:12ab"));
        assertTrue(processor.processCommand("REQUEST_PARTNER_ACCESS_CODE:Pizza Palace").startsWith("ERROR: Access code already sent."));
    }

    @Test
    void rejectsMalformedProductMutations() {
        ServerCommandProcessor processor = createProcessor();

        assertEquals("ERROR: Invalid product payload",
                processor.processCommand("ADD_PRODUCT:Burger Barn:{\"ProductName\":\"Burger\",\"ProductType\":\"Main\",\"AvailableAmount\":-1,\"Price\":9.99}"));
        assertEquals("ERROR: Invalid update values",
                processor.processCommand("UPDATE_PRODUCT:Burger Barn:Classic Burger:-1:2"));
        assertEquals("ERROR: Store name is required",
                processor.processCommand("REMOVE_STORE:   "));
    }

    private ServerCommandProcessor createProcessor() {
        List<StoreData> stores = new ArrayList<>();
        stores.add(new StoreData(
                "Burger Barn",
                1.0,
                2.0,
                "Burgers",
                4,
                10,
                "logo",
                new ProductData("Classic Burger", "Main", 5, 9.99)
        ));

        return new ServerCommandProcessor(
                stores,
                new PartnerAccessManager(java.util.Map.of("Pizza Palace", "ops@pizzapalace.foodie-demo.com")),
                () -> { }
        );
    }
}
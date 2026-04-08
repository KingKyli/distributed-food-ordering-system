import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StoreServiceTest {

    @Test
    void buyProductReducesInventoryAndPersists() {
        AtomicInteger persistCalls = new AtomicInteger();
        ProductData product = new ProductData("Burger", "Main", 5, 9.99);
        List<StoreData> stores = new ArrayList<>();
        stores.add(new StoreData("Burger Barn", 1.0, 2.0, "Burgers", 4, 10, "logo", product));

        StoreService service = new StoreService(stores, persistCalls::incrementAndGet);

        assertEquals(StoreService.MutationResult.SUCCESS, service.buyProduct("Burger Barn", "Burger", 2));
        assertEquals(3, product.availableAmount);
        assertEquals(1, persistCalls.get());
    }

    @Test
    void buyProductRejectsUnknownStoreAndInventoryProblems() {
        ProductData product = new ProductData("Burger", "Main", 1, 9.99);
        List<StoreData> stores = new ArrayList<>();
        stores.add(new StoreData("Burger Barn", 1.0, 2.0, "Burgers", 4, 10, "logo", product));

        StoreService service = new StoreService(stores, () -> { });

        assertEquals(StoreService.MutationResult.STORE_NOT_FOUND, service.buyProduct("Unknown", "Burger", 1));
        assertEquals(StoreService.MutationResult.INSUFFICIENT_INVENTORY, service.buyProduct("Burger Barn", "Burger", 2));
        assertEquals(StoreService.MutationResult.INVALID_QUANTITY, service.buyProduct("Burger Barn", "Burger", 0));
    }

    @Test
    void addUpdateAndRemoveProductFlowWorks() {
        AtomicInteger persistCalls = new AtomicInteger();
        List<StoreData> stores = new ArrayList<>();
        stores.add(new StoreData("Burger Barn", 1.0, 2.0, "Burgers", 4, 10, "logo"));

        StoreService service = new StoreService(stores, persistCalls::incrementAndGet);
        ProductData product = new ProductData("Burger", "Main", 5, 9.99);

        assertEquals(StoreService.MutationResult.SUCCESS, service.addProduct("Burger Barn", product));
        assertEquals(StoreService.MutationResult.PRODUCT_ALREADY_EXISTS, service.addProduct("Burger Barn", product));
        assertEquals(StoreService.MutationResult.SUCCESS, service.updateProduct("Burger Barn", "Burger", 12.50, 8));
        assertEquals(12.50, product.price);
        assertEquals(8, product.availableAmount);
        assertEquals(StoreService.MutationResult.SUCCESS, service.removeProduct("Burger Barn", "Burger"));
        assertEquals(StoreService.MutationResult.PRODUCT_NOT_FOUND, service.removeProduct("Burger Barn", "Burger"));
        assertEquals(3, persistCalls.get());
    }

    @Test
    void addAndRemoveStoreFlowWorks() {
        AtomicInteger persistCalls = new AtomicInteger();
        List<StoreData> stores = new ArrayList<>();
        StoreService service = new StoreService(stores, persistCalls::incrementAndGet);
        StoreData store = new StoreData("Burger Barn", 1.0, 2.0, "Burgers", 4, 10, "logo");

        assertEquals(StoreService.MutationResult.SUCCESS, service.addStore(store));
        assertEquals(StoreService.MutationResult.STORE_ALREADY_EXISTS, service.addStore(store));
        assertEquals(StoreService.MutationResult.SUCCESS, service.removeStore("Burger Barn"));
        assertEquals(StoreService.MutationResult.STORE_NOT_FOUND, service.removeStore("Burger Barn"));
        assertEquals(2, persistCalls.get());
    }
}
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StoreRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void saveStoresRollsBackWhenValidationFails() throws Exception {
        StoreRepository repository = new StoreRepository(tempDir, tempDir.resolve("restaurant-app.db"));
        repository.bootstrap(List.of(createStore("Pizza Palace", "Margherita", 4, 8.99)));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                repository.saveStores(List.of(
                        createStore("Burger Barn", "Classic Burger", 5, 9.99),
                        createStore("Burger Barn", "Cheese Burger", 3, 10.99)
                ))
        );

        assertEquals("Duplicate store names are not allowed: Burger Barn", error.getMessage());

        List<StoreData> persistedStores = repository.loadStores();
        assertEquals(1, persistedStores.size());
        assertEquals("Pizza Palace", persistedStores.get(0).storeName);
        assertEquals(1, persistedStores.get(0).products.size());
        assertEquals("Margherita", persistedStores.get(0).products.get(0).productName);
    }

    @Test
    void saveStoresRejectsNegativeProductValues() {
        StoreRepository repository = new StoreRepository(tempDir, tempDir.resolve("restaurant-app.db"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                repository.saveStores(List.of(createStore("Pizza Palace", "Margherita", -1, 8.99)))
        );

        assertEquals("Product inventory and price must not be negative", error.getMessage());
    }

    private StoreData createStore(String storeName, String productName, int amount, double price) {
        return new StoreData(
                storeName,
                1.0,
                2.0,
                "Category",
                4,
                10,
                "logo",
                new ProductData(productName, "Main", amount, price)
        );
    }
}
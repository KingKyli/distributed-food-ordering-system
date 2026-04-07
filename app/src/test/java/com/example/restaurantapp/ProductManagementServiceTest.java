package com.example.restaurantapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProductManagementServiceTest {

    @Test
    public void addProduct_rejectsMissingStore() {
        ProductManagementService service = new ProductManagementService(new StubRestaurantRepository(), new FakeServerGateway());

        AppResult<Void> result = service.addProduct(" ", new Product("Burger", "Food", 10, 5.0));

        assertFalse(result.isSuccess());
        assertEquals("Store info missing", result.getMessage());
    }

    @Test
    public void addProduct_returnsSuccess_whenGatewayAcceptsProduct() {
        ProductManagementService service = new ProductManagementService(
                new StubRestaurantRepository(),
                new FakeServerGateway().withAddProductResponse("OK: Product added")
        );

        AppResult<Void> result = service.addProduct("Burger Barn", new Product("Burger", "Food", 10, 5.0));

        assertTrue(result.isSuccess());
    }

    @Test
    public void updateProduct_returnsParsedError_whenGatewayRejectsUpdate() {
        ProductManagementService service = new ProductManagementService(
                new StubRestaurantRepository(),
                new FakeServerGateway().withUpdateProductResponse("ERROR: Product not found")
        );

        AppResult<Void> result = service.updateProduct("Burger Barn", "Missing Burger", 8.0, 5);

        assertFalse(result.isSuccess());
        assertEquals("Product not found", result.getMessage());
    }

    @Test
    public void removeProduct_propagatesTransportFailure() {
        ProductManagementService service = new ProductManagementService(
                new StubRestaurantRepository(),
                new FakeServerGateway().withRemoveProductResult(AppResult.error("Server offline"))
        );

        AppResult<Void> result = service.removeProduct("Burger Barn", "Burger");

        assertFalse(result.isSuccess());
        assertEquals("Server offline", result.getMessage());
    }
}
package com.example.restaurantapp;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OrderServiceTest {

    @Test
    public void submitOrder_rejectsEmptyBasket() {
        OrderService service = new OrderService(new FakeServerGateway(), new FakeOrderHistoryStore());

        AppResult<Void> result = service.submitOrder(Collections.emptyList());

        assertFalse(result.isSuccess());
        assertEquals("Your basket is empty.", result.getMessage());
    }

    @Test
    public void submitOrder_returnsTransportError_withoutSavingHistory() {
        FakeOrderHistoryStore orderHistoryStore = new FakeOrderHistoryStore();
        OrderService service = new OrderService(
                new FakeServerGateway().withBuyResult(AppResult.error("Server offline")),
                orderHistoryStore
        );

        AppResult<Void> result = service.submitOrder(singleItemOrder());

        assertFalse(result.isSuccess());
        assertEquals("Server offline", result.getMessage());
        assertTrue(orderHistoryStore.getSavedOrders().isEmpty());
    }

    @Test
    public void submitOrder_returnsFailedProducts_whenAnyItemIsRejected() {
        FakeOrderHistoryStore orderHistoryStore = new FakeOrderHistoryStore();
        FakeServerGateway gateway = new FakeServerGateway()
                .withBuyResponseForProduct("Burger", "SUCCESS: Order placed successfully")
                .withBuyResponseForProduct("Fries", "ERROR: Not enough inventory");
        OrderService service = new OrderService(gateway, orderHistoryStore);

        AppResult<Void> result = service.submitOrder(twoItemOrder());

        assertFalse(result.isSuccess());
        assertEquals("Some items could not be purchased: [Fries]", result.getMessage());
        assertTrue(orderHistoryStore.getSavedOrders().isEmpty());
    }

    @Test
    public void submitOrder_savesOrderHistory_whenAllItemsSucceed() {
        FakeOrderHistoryStore orderHistoryStore = new FakeOrderHistoryStore();
        OrderService service = new OrderService(
                new FakeServerGateway().withBuyResponse("SUCCESS: Order placed successfully"),
                orderHistoryStore
        );

        AppResult<Void> result = service.submitOrder(twoItemOrder());

        assertTrue(result.isSuccess());
        assertEquals(1, orderHistoryStore.getSavedOrders().size());
        OrderRecord saved = orderHistoryStore.getSavedOrders().get(0);
        assertEquals("Burger Barn", saved.getStoreName());
        assertEquals(2, saved.getItemSummaries().size());
        assertEquals(11.0, saved.getTotal(), 0.0001);
    }

    private static List<BasketItem> singleItemOrder() {
        return Collections.singletonList(new BasketItem("Burger Barn", "Burger", "Food", 2, 4.5));
    }

    private static List<BasketItem> twoItemOrder() {
        return Arrays.asList(
                new BasketItem("Burger Barn", "Burger", "Food", 2, 4.5),
                new BasketItem("Burger Barn", "Fries", "Food", 1, 2.0)
        );
    }
}
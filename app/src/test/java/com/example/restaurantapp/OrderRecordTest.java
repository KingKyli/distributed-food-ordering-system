package com.example.restaurantapp;

import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class OrderRecordTest {

    @Test
    public void constructor_buildsItemSummariesFromBasketItems() {
        OrderRecord record = new OrderRecord(
                "Burger Barn",
                Arrays.asList(
                        new BasketItem("Burger Barn", "Burger", "Food", 2, 4.5),
                        new BasketItem("Burger Barn", "Fries", "Food", 1, 2.0)
                ),
                11.0
        );

        assertEquals("Burger Barn", record.getStoreName());
        assertEquals(2, record.getItemSummaries().size());
        assertEquals("2x Burger", record.getItemSummaries().get(0));
        assertEquals("1x Fries", record.getItemSummaries().get(1));
        assertEquals(11.0, record.getTotal(), 0.0001);
    }

    @Test
    public void toJson_and_fromJson_roundTripPreservesOrderData() throws Exception {
        OrderRecord original = OrderRecord.fromPersisted(
                123456789L,
                "Pizza Palace",
                Arrays.asList("2x Margherita", "1x Cola"),
                18.5
        );

        JSONObject json = original.toJson();
        OrderRecord restored = OrderRecord.fromJson(json);

        assertEquals(123456789L, restored.getTimestamp());
        assertEquals("Pizza Palace", restored.getStoreName());
        assertEquals(2, restored.getItemSummaries().size());
        assertEquals("2x Margherita", restored.getItemSummaries().get(0));
        assertEquals("1x Cola", restored.getItemSummaries().get(1));
        assertEquals(18.5, restored.getTotal(), 0.0001);
    }
}
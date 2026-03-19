package com.example.restaurantapp;

import org.junit.Test;

import static org.junit.Assert.*;

public class BasketItemTest {

    @Test
    public void buildStableId_normalizesWhitespaceAndCase() {
        String stableId = BasketItem.buildStableId("  Pizza Palace ", " Margherita ", " PIZZA ");
        assertEquals("pizza palace::margherita::pizza", stableId);
    }

    @Test
    public void subtotal_isUnitPriceTimesQuantity() {
        BasketItem item = new BasketItem("Store", "Prod", "Type", 3, 4.5);
        assertEquals(13.5, item.getSubtotal(), 0.0001);
    }
}

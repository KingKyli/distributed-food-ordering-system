package com.example.restaurantapp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BasketTest {

    private Basket basket;

    @Before
    public void setUp() {
        basket = Basket.getInstance();
        basket.clear();
    }

    @Test
    public void addProduct_firstAddSetsStoreName() {
        Product p = new Product("Burger", "Food", 10, 5.0);
        assertTrue(basket.addProduct(p, "Burger Barn"));
        assertEquals("Burger Barn", basket.getStoreName());
        assertEquals(1, basket.getItemCount());
    }

    @Test
    public void addProduct_rejectsDifferentStore() {
        Product p1 = new Product("Burger", "Food", 10, 5.0);
        Product p2 = new Product("Pizza", "Food", 10, 7.0);

        assertTrue(basket.addProduct(p1, "Burger Barn"));
        assertFalse(basket.addProduct(p2, "Pizza Palace"));

        assertEquals("Burger Barn", basket.getStoreName());
        assertEquals(1, basket.getItemCount());
        assertEquals(5.0, basket.getTotalPrice(), 0.0001);
    }

    @Test
    public void addProduct_sameProductIncrementsQuantityAndTotal() {
        Product p = new Product("Soda", "Drink", 10, 2.0);

        assertTrue(basket.addProduct(p, "Taco Town"));
        assertTrue(basket.addProduct(p, "Taco Town"));
        assertTrue(basket.addProduct(p, "Taco Town"));

        assertEquals(3, basket.getItemCount());
        assertEquals(6.0, basket.getTotalPrice(), 0.0001);
        assertEquals(1, basket.getItems().size());
        assertEquals(3, basket.getItems().get(0).getQuantity());
    }

    @Test
    public void removeProduct_decrementsAndClearsStoreWhenEmpty() {
        Product p = new Product("Sushi", "Food", 10, 12.0);
        assertTrue(basket.addProduct(p, "Sushi Sun"));
        assertTrue(basket.addProduct(p, "Sushi Sun"));

        basket.removeProduct(p);
        assertEquals(1, basket.getItemCount());
        assertEquals("Sushi Sun", basket.getStoreName());

        basket.removeProduct(p);
        assertEquals(0, basket.getItemCount());
        assertTrue(basket.isEmpty());
        assertNull(basket.getStoreName());
    }
}

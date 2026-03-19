// src/main/java/com/example/restaurantapp/Basket.java
package com.example.restaurantapp;

import java.util.HashMap;
import java.util.Map;

public class Basket {
    private static Basket instance;
    private final Map<Product, Integer> items = new HashMap<>();

    private Basket() {}
    private String storeName = null;

    public String getStoreName() {
        return storeName;
    }


    public static Basket getInstance() {
        if (instance == null) {
            instance = new Basket();
        }
        return instance;
    }

    public void addProduct(Product product) {
        int count = items.getOrDefault(product, 0);
        items.put(product, count + 1);
    }

    public boolean addProduct(Product product, String storeName) {
        if (items.isEmpty()) {
            this.storeName = storeName;
        }
        if (this.storeName != null && !this.storeName.equals(storeName)) {
            // Reject addition from a different store
            return false;
        }
        int count = items.getOrDefault(product, 0);
        items.put(product, count + 1);
        return true;
    }

    // In Basket.java
    public void removeProduct(Product product) {
        if (!items.containsKey(product)) return;
        int count = items.get(product);
        if (count > 1) {
            items.put(product, count - 1);
        } else {
            items.remove(product);
            if (items.isEmpty()) {
                storeName = null;
            }
        }
    }

    public Map<Product, Integer> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
        storeName = null;
    }
}
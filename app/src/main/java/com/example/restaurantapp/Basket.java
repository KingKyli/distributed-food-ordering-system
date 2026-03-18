// src/main/java/com/example/restaurantapp/Basket.java
package com.example.restaurantapp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Basket {
    private static volatile Basket instance;
    private final Map<Product, Integer> items = new HashMap<>();

    private Basket() {}
    private String storeName = null;

    public String getStoreName() {
        return storeName;
    }

    public static Basket getInstance() {
        if (instance == null) {
            synchronized (Basket.class) {
                if (instance == null) {
                    instance = new Basket();
                }
            }
        }
        return instance;
    }

    public synchronized void addProduct(Product product) {
        int count = items.getOrDefault(product, 0);
        items.put(product, count + 1);
    }

    public synchronized boolean addProduct(Product product, String storeName) {
        if (items.isEmpty()) {
            this.storeName = storeName;
        }
        if (!this.storeName.equals(storeName)) {
            // Reject addition from a different store
            return false;
        }
        int count = items.getOrDefault(product, 0);
        items.put(product, count + 1);
        return true;
    }

    public synchronized void removeProduct(Product product) {
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

    public synchronized Map<Product, Integer> getItems() {
        return Collections.unmodifiableMap(new HashMap<>(items));
    }

    public synchronized void clear() {
        items.clear();
        storeName = null;
    }
}
package com.example.restaurantapp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Basket {
    private static Basket instance;
    private final LinkedHashMap<String, BasketItem> items = new LinkedHashMap<>();

    private Basket() {
    }

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

    public void addProduct(Product product) {
        addProduct(product, storeName);
    }

    public synchronized boolean addProduct(Product product, String storeName) {
        if (product == null || storeName == null || storeName.trim().isEmpty()) {
            return false;
        }
        if (items.isEmpty()) {
            this.storeName = storeName;
        }
        if (this.storeName == null || !this.storeName.equals(storeName)) {
            return false;
        }

        String stableId = BasketItem.buildStableId(storeName, product.getProductName(), product.getProductType());
        BasketItem item = items.get(stableId);
        if (item == null) {
            items.put(stableId, new BasketItem(storeName, product.getProductName(), product.getProductType(), 1, product.getPrice()));
        } else {
            item.setQuantity(item.getQuantity() + 1);
            item.setUnitPrice(product.getPrice());
        }
        return true;
    }

    public synchronized void removeProduct(Product product) {
        if (product == null) {
            return;
        }
        removeProductById(BasketItem.buildStableId(storeName, product.getProductName(), product.getProductType()));
    }

    public synchronized void removeProductById(String stableId) {
        BasketItem item = items.get(stableId);
        if (item == null) {
            return;
        }
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        } else {
            items.remove(stableId);
            if (items.isEmpty()) {
                storeName = null;
            }
        }
    }

    public synchronized List<BasketItem> getItems() {
        List<BasketItem> snapshot = new ArrayList<>(items.size());
        for (BasketItem item : items.values()) {
            snapshot.add(new BasketItem(
                    item.getStoreName(),
                    item.getProductName(),
                    item.getProductType(),
                    item.getQuantity(),
                    item.getUnitPrice()
            ));
        }
        return snapshot;
    }

    public synchronized boolean isEmpty() {
        return items.isEmpty();
    }

    public synchronized int getItemCount() {
        int count = 0;
        for (BasketItem item : items.values()) {
            count += item.getQuantity();
        }
        return count;
    }

    public synchronized double getTotalPrice() {
        double total = 0;
        for (BasketItem item : items.values()) {
            total += item.getSubtotal();
        }
        return total;
    }

    public synchronized Map<String, BasketItem> getItemsById() {
        return items;
    }

    public synchronized void clear() {
        items.clear();
        storeName = null;
    }
}
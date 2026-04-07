package com.example.restaurantapp;

import java.util.ArrayList;
import java.util.List;

public class FakeOrderHistoryStore implements OrderHistoryStore {
    private final List<OrderRecord> savedOrders = new ArrayList<>();

    @Override
    public void saveOrder(OrderRecord record) {
        savedOrders.add(record);
    }

    public List<OrderRecord> getSavedOrders() {
        return savedOrders;
    }
}
package com.example.restaurantapp;

public interface OrderHistoryStore {
    void saveOrder(OrderRecord record);
}
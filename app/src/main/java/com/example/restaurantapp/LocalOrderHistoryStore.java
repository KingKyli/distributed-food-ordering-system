package com.example.restaurantapp;

import android.content.Context;

public class LocalOrderHistoryStore implements OrderHistoryStore {
    private final Context appContext;

    public LocalOrderHistoryStore(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void saveOrder(OrderRecord record) {
        OrderHistoryRepository.saveOrder(appContext, record);
    }
}
package com.example.restaurantapp;

import java.util.ArrayList;
import java.util.List;

public class OrderService {

    public AppResult<Void> submitOrder(List<BasketItem> items) {
        MasterCommunicator communicator = ServerConnection.getInstance();
        if (communicator == null) {
            return AppResult.error("Server connection is not available.");
        }

        if (items == null || items.isEmpty()) {
            return AppResult.error("Your basket is empty.");
        }

        List<String> failed = new ArrayList<>();
        for (BasketItem item : items) {
            boolean ok = communicator.sendBuyRequest(item.getStoreName(), item.getProductName(), item.getQuantity());
            if (!ok) {
                failed.add(item.getProductName());
            }
        }

        if (!failed.isEmpty()) {
            return AppResult.error("Some items could not be purchased.");
        }
        return AppResult.success(null);
    }
}


package com.example.restaurantapp;

import java.util.ArrayList;
import java.util.List;

public class OrderService {

    public AppResult<Void> submitOrder(List<BasketItem> items) {
        AppResult<MasterCommunicator> communicatorResult = ServerConnection.requireCommunicator();
        if (!communicatorResult.isSuccess()) {
            return AppResult.error(communicatorResult.getMessage());
        }
        MasterCommunicator communicator = communicatorResult.getData();

        if (items == null || items.isEmpty()) {
            return AppResult.error("Your basket is empty.");
        }

        List<String> failed = new ArrayList<>();
        for (BasketItem item : items) {
            String response = communicator.sendBuyRequestDetailed(item.getStoreName(), item.getProductName(), item.getQuantity());
            if (!ProtocolUtils.isOkResponse(response)) {
                failed.add(item.getProductName());
            }
        }

        if (!failed.isEmpty()) {
            return AppResult.error("Some items could not be purchased: " + failed);
        }
        return AppResult.success(null);
    }
}


package com.example.restaurantapp;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final Context appContext;

    public OrderService(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public AppResult<Void> submitOrder(List<BasketItem> items) {
        AppResult<MasterCommunicator> communicatorResult = ServerConnection.requireCommunicator();
        if (!communicatorResult.isSuccess()) {
            return AppResult.error(communicatorResult.getMessage());
        }
        MasterCommunicator communicator = communicatorResult.getData();

        if (items == null || items.isEmpty()) {
            return AppResult.error("Your basket is empty.");
        }

        String storeName = items.get(0).getStoreName();
        double total = 0;
        for (BasketItem item : items) total += item.getSubtotal();

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

        // Persist to local order history
        OrderRecord record = new OrderRecord(storeName, items, total);
        OrderHistoryRepository.saveOrder(appContext, record);

        return AppResult.success(null);
    }
}

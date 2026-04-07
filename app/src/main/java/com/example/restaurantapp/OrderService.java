package com.example.restaurantapp;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private final ServerGateway serverGateway;
    private final OrderHistoryStore orderHistoryStore;

    public OrderService(Context context) {
        this(new TcpServerGateway(), new LocalOrderHistoryStore(context));
    }

    OrderService(ServerGateway serverGateway, OrderHistoryStore orderHistoryStore) {
        this.serverGateway = serverGateway;
        this.orderHistoryStore = orderHistoryStore;
    }

    public AppResult<Void> submitOrder(List<BasketItem> items) {
        if (items == null || items.isEmpty()) {
            return AppResult.error("Your basket is empty.");
        }

        String storeName = items.get(0).getStoreName();
        double total = 0;
        for (BasketItem item : items) total += item.getSubtotal();

        List<String> failed = new ArrayList<>();
        for (BasketItem item : items) {
            AppResult<String> responseResult = serverGateway.buy(item.getStoreName(), item.getProductName(), item.getQuantity());
            if (!responseResult.isSuccess()) {
                return AppResult.error(responseResult.getMessage());
            }
            String response = responseResult.getData();
            if (!ProtocolUtils.isOkResponse(response)) {
                failed.add(item.getProductName());
            }
        }

        if (!failed.isEmpty()) {
            return AppResult.error("Some items could not be purchased: " + failed);
        }

        // Persist to local order history
        OrderRecord record = new OrderRecord(storeName, items, total);
        orderHistoryStore.saveOrder(record);

        return AppResult.success(null);
    }
}

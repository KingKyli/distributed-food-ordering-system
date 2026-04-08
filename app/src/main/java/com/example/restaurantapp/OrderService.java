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
            AppResult<String> responseResult = serverGateway.buy(
                    item.getStoreName(), item.getProductName(), item.getQuantity());
            // Transport error → abort immediately, no history saved
            if (!responseResult.isSuccess()) {
                return AppResult.error(responseResult.getMessage());
            }
            if (!ProtocolUtils.isOkResponse(responseResult.getData())) {
                failed.add(item.getProductName());
            }
        }

        if (!failed.isEmpty()) {
            // Some items were rejected by the server → save as CANCELLED so the user
            // can see what went wrong in Order History.
            OrderRecord cancelled = OrderRecord.withStatus(storeName, items, total,
                    OrderRecord.OrderStatus.CANCELLED);
            orderHistoryStore.saveOrder(cancelled);
            return AppResult.error("Some items could not be purchased: " + failed);
        }

        // All items accepted → save as DELIVERED
        OrderRecord record = new OrderRecord(storeName, items, total);
        orderHistoryStore.saveOrder(record);

        return AppResult.success(null);
    }
}

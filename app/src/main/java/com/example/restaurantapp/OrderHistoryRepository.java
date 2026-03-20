package com.example.restaurantapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Persists order history locally. Keeps the last {@link #MAX_ORDERS} orders. */
public final class OrderHistoryRepository {
    private static final String PREFS_NAME = "order_history_prefs";
    private static final String KEY_ORDERS = "orders";
    private static final int MAX_ORDERS   = 30;

    private OrderHistoryRepository() {}

    public static void saveOrder(Context context, OrderRecord record) {
        List<OrderRecord> orders = getOrders(context);
        orders.add(0, record); // newest first
        if (orders.size() > MAX_ORDERS) {
            orders = orders.subList(0, MAX_ORDERS);
        }
        JSONArray array = new JSONArray();
        for (OrderRecord r : orders) {
            try { array.put(r.toJson()); } catch (Exception ignored) {}
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_ORDERS, array.toString()).apply();
    }

    public static List<OrderRecord> getOrders(Context context) {
        String json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ORDERS, "[]");
        List<OrderRecord> orders = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                try {
                    orders.add(OrderRecord.fromJson(array.getJSONObject(i)));
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return orders;
    }

    public static void clearAll(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().remove(KEY_ORDERS).apply();
    }
}


package com.example.restaurantapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/** Persists order history locally. Keeps the last {@link #MAX_ORDERS} orders. */
public final class OrderHistoryRepository {
    private static final String PREFS_NAME = "order_history_prefs";
    private static final String KEY_ORDERS = "orders";
    private static final int MAX_ORDERS   = 30;

    private OrderHistoryRepository() {}

    public static void saveOrder(Context context, OrderRecord record) {
        OrderHistoryDao dao = getDao(context);
        migrateLegacyOrdersIfNeeded(context, dao);
        dao.insert(OrderRecordEntity.fromOrderRecord(record));
        dao.trimToLimit(MAX_ORDERS);
    }

    public static List<OrderRecord> getOrders(Context context) {
        OrderHistoryDao dao = getDao(context);
        migrateLegacyOrdersIfNeeded(context, dao);
        List<OrderRecord> orders = new ArrayList<>();
        for (OrderRecordEntity entity : dao.getRecentOrders(MAX_ORDERS)) {
            orders.add(entity.toOrderRecord());
        }
        return orders;
    }

    public static void clearAll(Context context) {
        getDao(context).clearAll();
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().remove(KEY_ORDERS).apply();
    }

    private static OrderHistoryDao getDao(Context context) {
        return RestaurantAppDatabase.getInstance(context).orderHistoryDao();
    }

    private static void migrateLegacyOrdersIfNeeded(Context context, OrderHistoryDao dao) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_ORDERS, null);
        if (json == null || json.trim().isEmpty()) {
            return;
        }

        try {
            JSONArray array = new JSONArray(json);
            for (int index = 0; index < array.length(); index++) {
                dao.insert(OrderRecordEntity.fromOrderRecord(OrderRecord.fromJson(array.getJSONObject(index))));
            }
            dao.trimToLimit(MAX_ORDERS);
        } catch (Exception ignored) {
        }

        prefs.edit().remove(KEY_ORDERS).apply();
    }
}


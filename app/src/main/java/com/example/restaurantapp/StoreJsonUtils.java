package com.example.restaurantapp;

import org.json.JSONObject;

public final class StoreJsonUtils {
    private StoreJsonUtils() {
    }

    public static String extractStoreName(String storeJson) {
        if (storeJson == null || storeJson.trim().isEmpty()) {
            return null;
        }
        try {
            JSONObject obj = new JSONObject(storeJson);
            String name = obj.optString("StoreName", null);
            return name == null ? null : name.trim();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static AppResult<Store> parseStore(String storeJson) {
        if (storeJson == null || storeJson.trim().isEmpty()) {
            return AppResult.error("Store details are not available.");
        }
        try {
            return AppResult.success(Store.fromJson(new JSONObject(storeJson)));
        } catch (Exception e) {
            return AppResult.error("Error loading store details.");
        }
    }
}

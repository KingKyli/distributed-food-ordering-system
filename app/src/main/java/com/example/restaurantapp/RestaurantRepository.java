package com.example.restaurantapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RestaurantRepository {

    public AppResult<List<Store>> searchStores(String latitude, String longitude, String cuisine, int stars, String price) {
        MasterCommunicator communicator = ServerConnection.getInstance();
        if (communicator == null) {
            return AppResult.error("Server connection is not available.");
        }

        String response = communicator.sendSearchRequest(
                safe(latitude),
                safe(longitude),
                safe(cuisine),
                stars > 0 ? String.valueOf(stars) : "",
                safe(price)
        );

        if (response == null || response.trim().isEmpty()) {
            return AppResult.error("Could not load restaurants from the server.");
        }

        try {
            return AppResult.success(MainActivity.parseStores(response));
        } catch (Exception e) {
            return AppResult.error("Error loading restaurants: " + e.getMessage());
        }
    }

    public AppResult<Store> fetchStoreByName(String storeName) {
        AppResult<List<Store>> result = searchStores("", "", "", 0, "");
        if (!result.isSuccess()) {
            return AppResult.error(result.getMessage());
        }

        if (storeName == null || storeName.trim().isEmpty()) {
            return AppResult.error("Store name is missing.");
        }

        for (Store store : result.getData()) {
            if (store != null && storeName.equalsIgnoreCase(store.getStoreName())) {
                return AppResult.success(store);
            }
        }
        return AppResult.error("Store not found.");
    }

    public AppResult<List<Product>> fetchStoreProducts(String storeName) {
        AppResult<Store> storeResult = fetchStoreByName(storeName);
        if (!storeResult.isSuccess()) {
            return AppResult.error(storeResult.getMessage());
        }
        List<Product> products = storeResult.getData().getProducts();
        return AppResult.success(products != null ? products : new ArrayList<>());
    }

    public AppResult<List<String>> fetchStoreNames() {
        AppResult<List<Store>> result = searchStores("", "", "", 0, "");
        if (!result.isSuccess()) {
            return AppResult.error(result.getMessage());
        }

        List<String> names = new ArrayList<>();
        for (Store store : result.getData()) {
            if (store != null && store.getStoreName() != null) {
                names.add(store.getStoreName());
            }
        }
        return AppResult.success(names);
    }

    public AppResult<Store> parseStoreJson(String storeJson) {
        if (storeJson == null || storeJson.trim().isEmpty()) {
            return AppResult.error("Store details are not available.");
        }
        try {
            return AppResult.success(Store.fromJson(new JSONObject(storeJson)));
        } catch (Exception e) {
            return AppResult.error("Error loading store details.");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}


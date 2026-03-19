package com.example.restaurantapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class StoreJsonParser {
    private StoreJsonParser() {
    }

    public static List<Store> parseStores(String jsonString) throws JSONException {
        List<Store> stores = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            stores.add(Store.fromJson(obj));
        }
        return stores;
    }
}

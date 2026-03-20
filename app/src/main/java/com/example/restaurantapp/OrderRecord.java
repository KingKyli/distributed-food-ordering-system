package com.example.restaurantapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Immutable record of a completed order, serialisable to/from JSON. */
public final class OrderRecord {
    private long timestamp;
    private String storeName;
    private List<String> itemSummaries; // e.g. "2x Margherita"
    private double total;

    private OrderRecord() {}

    public OrderRecord(String storeName, List<BasketItem> items, double total) {
        this.timestamp = System.currentTimeMillis();
        this.storeName = storeName;
        this.total = total;
        this.itemSummaries = new ArrayList<>();
        if (items != null) {
            for (BasketItem item : items) {
                itemSummaries.add(item.getQuantity() + "x " + item.getProductName());
            }
        }
    }

    public long getTimestamp()             { return timestamp; }
    public String getStoreName()           { return storeName; }
    public List<String> getItemSummaries() { return itemSummaries; }
    public double getTotal()               { return total; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("timestamp", timestamp);
        obj.put("storeName", storeName);
        obj.put("total", total);
        JSONArray arr = new JSONArray();
        if (itemSummaries != null) {
            for (String s : itemSummaries) arr.put(s);
        }
        obj.put("items", arr);
        return obj;
    }

    public static OrderRecord fromJson(JSONObject obj) throws JSONException {
        OrderRecord r = new OrderRecord();
        r.timestamp = obj.getLong("timestamp");
        r.storeName = obj.getString("storeName");
        r.total     = obj.getDouble("total");
        r.itemSummaries = new ArrayList<>();
        JSONArray arr = obj.optJSONArray("items");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) r.itemSummaries.add(arr.getString(i));
        }
        return r;
    }
}


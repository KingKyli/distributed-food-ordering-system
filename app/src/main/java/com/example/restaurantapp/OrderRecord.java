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

    private OrderRecord(long timestamp, String storeName, List<String> itemSummaries, double total) {
        this.timestamp = timestamp;
        this.storeName = storeName;
        this.total = total;
        this.itemSummaries = itemSummaries != null ? new ArrayList<>(itemSummaries) : new ArrayList<>();
    }

    public OrderRecord(String storeName, List<BasketItem> items, double total) {
        this(System.currentTimeMillis(), storeName, new ArrayList<>(), total);
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
        long timestamp = obj.getLong("timestamp");
        String storeName = obj.getString("storeName");
        double total = obj.getDouble("total");
        List<String> itemSummaries = new ArrayList<>();
        JSONArray arr = obj.optJSONArray("items");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) itemSummaries.add(arr.getString(i));
        }
        return new OrderRecord(timestamp, storeName, itemSummaries, total);
    }

    public static OrderRecord fromPersisted(long timestamp, String storeName, List<String> itemSummaries, double total) {
        return new OrderRecord(timestamp, storeName, itemSummaries, total);
    }
}


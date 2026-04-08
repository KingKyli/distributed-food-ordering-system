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

    public enum OrderStatus { DELIVERED, PENDING, CANCELLED }

    private long timestamp;
    private String storeName;
    private List<String> itemSummaries; // e.g. "2x Margherita"
    private double total;
    private OrderStatus status;
    private String orderId;

    private OrderRecord() {}

    private OrderRecord(long timestamp, String storeName, List<String> itemSummaries,
                        double total, OrderStatus status, String orderId) {
        this.timestamp    = timestamp;
        this.storeName    = storeName;
        this.total        = total;
        this.itemSummaries = itemSummaries != null ? new ArrayList<>(itemSummaries) : new ArrayList<>();
        this.status       = status != null ? status : OrderStatus.DELIVERED;
        this.orderId      = orderId != null ? orderId : generateOrderId(timestamp);
    }

    public OrderRecord(String storeName, List<BasketItem> items, double total) {
        this(System.currentTimeMillis(), storeName, new ArrayList<>(),
             total, OrderStatus.DELIVERED, null);
        if (items != null) {
            for (BasketItem item : items) {
                itemSummaries.add(item.getQuantity() + "x " + item.getProductName());
            }
        }
    }

    /** Creates a record with an explicit status (e.g. CANCELLED for rejected orders). */
    public static OrderRecord withStatus(String storeName, List<BasketItem> items,
                                         double total, OrderStatus status) {
        long ts = System.currentTimeMillis();
        List<String> summaries = new ArrayList<>();
        if (items != null) {
            for (BasketItem item : items) {
                summaries.add(item.getQuantity() + "x " + item.getProductName());
            }
        }
        return new OrderRecord(ts, storeName, summaries, total, status, null);
    }

    private static String generateOrderId(long timestamp) {
        return String.valueOf(10000 + (int)(timestamp % 90000));
    }

    public long getTimestamp()             { return timestamp; }
    public String getStoreName()           { return storeName; }
    public List<String> getItemSummaries() { return itemSummaries; }
    public double getTotal()               { return total; }
    public OrderStatus getStatus()         { return status; }
    public String getOrderId()             { return orderId; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("timestamp", timestamp);
        obj.put("storeName", storeName);
        obj.put("total", total);
        obj.put("status", status != null ? status.name() : OrderStatus.DELIVERED.name());
        obj.put("orderId", orderId != null ? orderId : "");
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
        String statusStr = obj.optString("status", OrderStatus.DELIVERED.name());
        OrderStatus status;
        try { status = OrderStatus.valueOf(statusStr); } catch (Exception e) { status = OrderStatus.DELIVERED; }
        String orderId = obj.optString("orderId", generateOrderId(timestamp));
        List<String> itemSummaries = new ArrayList<>();
        JSONArray arr = obj.optJSONArray("items");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) itemSummaries.add(arr.getString(i));
        }
        return new OrderRecord(timestamp, storeName, itemSummaries, total, status, orderId);
    }

    public static OrderRecord fromPersisted(long timestamp, String storeName, List<String> itemSummaries,
                                            double total, OrderRecord.OrderStatus status, String orderId) {
        return new OrderRecord(timestamp, storeName, itemSummaries, total, status, orderId);
    }

    /** Legacy overload – keeps old call-sites compiling; defaults to DELIVERED. */
    public static OrderRecord fromPersisted(long timestamp, String storeName,
                                            List<String> itemSummaries, double total) {
        return new OrderRecord(timestamp, storeName, itemSummaries, total,
                OrderStatus.DELIVERED, generateOrderId(timestamp));
    }
}


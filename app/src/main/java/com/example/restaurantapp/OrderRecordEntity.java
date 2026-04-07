package com.example.restaurantapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "order_history")
public class OrderRecordEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @NonNull
    @ColumnInfo(name = "store_name")
    public String storeName;

    @NonNull
    @ColumnInfo(name = "item_summaries_json")
    public String itemSummariesJson;

    @ColumnInfo(name = "total")
    public double total;

    public static OrderRecordEntity fromOrderRecord(OrderRecord record) {
        OrderRecordEntity entity = new OrderRecordEntity();
        entity.timestamp = record.getTimestamp();
        entity.storeName = record.getStoreName() != null ? record.getStoreName() : "";
        entity.total = record.getTotal();

        JSONArray array = new JSONArray();
        List<String> itemSummaries = record.getItemSummaries();
        if (itemSummaries != null) {
            for (String itemSummary : itemSummaries) {
                array.put(itemSummary);
            }
        }
        entity.itemSummariesJson = array.toString();
        return entity;
    }

    public OrderRecord toOrderRecord() {
        return OrderRecord.fromPersisted(timestamp, storeName, parseItemSummaries(itemSummariesJson), total);
    }

    private static List<String> parseItemSummaries(String itemSummariesJson) {
        List<String> itemSummaries = new ArrayList<>();
        if (itemSummariesJson == null || itemSummariesJson.trim().isEmpty()) {
            return itemSummaries;
        }

        try {
            JSONArray array = new JSONArray(itemSummariesJson);
            for (int index = 0; index < array.length(); index++) {
                itemSummaries.add(array.getString(index));
            }
        } catch (Exception ignored) {
        }
        return itemSummaries;
    }
}
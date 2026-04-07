package com.example.restaurantapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OrderHistoryDao {
    @Insert
    void insert(OrderRecordEntity entity);

    @Query("SELECT * FROM order_history ORDER BY timestamp DESC LIMIT :limit")
    List<OrderRecordEntity> getRecentOrders(int limit);

    @Query("DELETE FROM order_history")
    void clearAll();

    @Query("DELETE FROM order_history WHERE id NOT IN (SELECT id FROM order_history ORDER BY timestamp DESC LIMIT :limit)")
    void trimToLimit(int limit);
}
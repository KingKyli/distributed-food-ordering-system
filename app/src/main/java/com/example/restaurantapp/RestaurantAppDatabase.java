package com.example.restaurantapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {OrderRecordEntity.class}, version = 1, exportSchema = false)
public abstract class RestaurantAppDatabase extends RoomDatabase {
    private static volatile RestaurantAppDatabase instance;

    public abstract OrderHistoryDao orderHistoryDao();

    public static RestaurantAppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (RestaurantAppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    RestaurantAppDatabase.class,
                                    "restaurant_app.db"
                            )
                            .build();
                }
            }
        }
        return instance;
    }
}
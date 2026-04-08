package com.example.restaurantapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {OrderRecordEntity.class}, version = 2, exportSchema = false)
public abstract class RestaurantAppDatabase extends RoomDatabase {
    private static volatile RestaurantAppDatabase instance;

    /** Adds the status and order_id columns introduced in v2. */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE order_history ADD COLUMN status TEXT NOT NULL DEFAULT 'DELIVERED'");
            db.execSQL("ALTER TABLE order_history ADD COLUMN order_id TEXT NOT NULL DEFAULT ''");
        }
    };

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
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return instance;
    }
}
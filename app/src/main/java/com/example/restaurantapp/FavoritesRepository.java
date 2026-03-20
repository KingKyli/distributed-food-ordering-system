package com.example.restaurantapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/** Stores favourite store names locally in SharedPreferences. */
public final class FavoritesRepository {
    private static final String PREFS_NAME = "favorites_prefs";
    private static final String KEY_FAVORITES = "favorite_stores";

    private FavoritesRepository() {}

    public static boolean isFavorite(Context context, String storeName) {
        if (storeName == null) return false;
        return getFavorites(context).contains(storeName.trim());
    }

    public static boolean toggleFavorite(Context context, String storeName) {
        if (storeName == null) return false;
        Set<String> favorites = new HashSet<>(getFavorites(context));
        boolean nowFavorite;
        if (favorites.contains(storeName.trim())) {
            favorites.remove(storeName.trim());
            nowFavorite = false;
        } else {
            favorites.add(storeName.trim());
            nowFavorite = true;
        }
        saveFavorites(context, favorites);
        return nowFavorite;
    }

    public static Set<String> getFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> stored = prefs.getStringSet(KEY_FAVORITES, null);
        return stored != null ? new HashSet<>(stored) : new HashSet<>();
    }

    private static void saveFavorites(Context context, Set<String> favorites) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }
}


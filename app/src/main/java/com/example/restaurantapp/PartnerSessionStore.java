package com.example.restaurantapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

public final class PartnerSessionStore {
    private static final String TAG = "PartnerSessionStore";
    private static final String PREFS_NAME = "partner_session";
    private static final String KEY_STORE_NAME = "store_name";
    private static final String KEY_STORE_JSON = "store_json";
    private static final String KEY_LOGIN_TIMESTAMP = "login_timestamp";

    private PartnerSessionStore() {
    }

    public static void saveSession(Context context, Store store) {
        if (context == null || store == null || store.toJson() == null) {
            return;
        }
        saveSession(context, store.getStoreName(), store.toJson().toString());
    }

    public static void saveSession(Context context, String storeName, String storeJson) {
        if (context == null) {
            return;
        }
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putString(KEY_STORE_NAME, storeName)
                .putString(KEY_STORE_JSON, storeJson)
                .putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    public static boolean hasActiveSession(Context context) {
        return !TextUtils.isEmpty(getStoreJson(context)) || !TextUtils.isEmpty(getStoreName(context));
    }

    public static String getStoreName(Context context) {
        if (context == null) {
            return null;
        }
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_STORE_NAME, null);
    }

    public static String getStoreJson(Context context) {
        if (context == null) {
            return null;
        }
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_STORE_JSON, null);
    }

    public static Store getStore(Context context) {
        String storeJson = getStoreJson(context);
        if (TextUtils.isEmpty(storeJson)) {
            return null;
        }
        try {
            return Store.fromJson(new JSONObject(storeJson));
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore saved partner session.", e);
            clear(context);
            return null;
        }
    }

    public static void clear(Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }
}


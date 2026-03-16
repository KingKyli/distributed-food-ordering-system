package com.example.restaurantapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class PartnerLoginManager {
    private static PartnerLoginManager instance;
    private final Map<String, String> storePasswords = new HashMap<>();
    private MasterCommunicator communicator;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "partner_passwords";

    private PartnerLoginManager() {
    }

    public static PartnerLoginManager getInstance() {
        if (instance == null) {
            instance = new PartnerLoginManager();
        }
        return instance;
    }

    public void init(Context context) {
        if (sharedPreferences == null && context != null) {
            sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            // Load all stored passwords into memory
            Map<String, ?> all = sharedPreferences.getAll();
            for (Map.Entry<String, ?> entry : all.entrySet()) {
                storePasswords.put(entry.getKey(), entry.getValue().toString());
                android.util.Log.d("PartnerLoginManager", "Your Password is: " + entry.getKey());
                android.util.Log.d("PartnerLoginManager", "Your Password is: " + entry.getValue().toString());
            }
        }
    }

    // Generates a password: storeName + 5 random alphanumeric characters
    public static String generateStorePassword(String storeName) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789,.?!@#";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(storeName);
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public String getOrGeneratePassword(String storeName, Context context) {
        init(context);
        if (!storePasswords.containsKey(storeName)) {
            String password = generateStorePassword(storeName);
            android.util.Log.d("PartnerLoginManager", "Your Password is: " + password);
            storePasswords.put(storeName, password);
            if (sharedPreferences != null) {
                sharedPreferences.edit().putString(storeName, password).apply();
            }
            if (context != null) {
                Toast.makeText(context, "Password for " + storeName + ": " + password, Toast.LENGTH_LONG).show();
            }
        }
        return storePasswords.get(storeName);
    }
}

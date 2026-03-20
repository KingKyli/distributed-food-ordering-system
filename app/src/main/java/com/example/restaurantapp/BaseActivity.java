package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract String getBottomNavType();  // "home", "filters", "orders", "settings"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ServerConnection.init("192.168.56.1", 5000);
    }

    protected void setupBottomNav() {
        TextView navHome = findViewById(R.id.nav_home);
        TextView navFilters = findViewById(R.id.nav_filters);
        TextView navOrders = findViewById(R.id.nav_orders);
        TextView navSettings = findViewById(R.id.nav_settings);
        // Highlight the current section
        String type = getBottomNavType();
        int activeColor = Color.parseColor("#726EFF"); // primary color
        if ("home".equals(type)) {
            navHome.setTextColor(activeColor);
        } else if ("filters".equals(type)) {
            navFilters.setTextColor(activeColor);
        } else if ("orders".equals(type)) {
            navOrders.setTextColor(activeColor);
        } else if ("settings".equals(type)) {
            navSettings.setTextColor(activeColor);
        }

        navHome.setOnClickListener(v -> {
            if (!(this instanceof MainActivity)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        navFilters.setOnClickListener(v -> {
            if (!(this instanceof FiltersActivity)) {
                startActivity(new Intent(this, FiltersActivity.class));
                finish();
            }
        });
        navOrders.setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));
        navSettings.setOnClickListener(v -> {
            if (!(this instanceof SettingsActivity)) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
            }
        });
    }
}
package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.restaurantapp.MainActivity;
import com.example.restaurantapp.FiltersActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract String getBottomNavType();  // "home" or "bookings"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ServerConnection.init("192.168.56.1", 5000);
    }

    protected void setupBottomNav() {
        TextView navHome = findViewById(R.id.nav_home);
        TextView navFilters = findViewById(R.id.nav_filters);
        TextView navSettings = findViewById(R.id.nav_settings);
        // Highlight the current section
        String type = getBottomNavType();
        if ("home".equals(type)) {
            navHome.setTextColor(Color.parseColor("#726EFF"));
        } else if ("filters".equals(type)) {
            navFilters.setTextColor(Color.parseColor("#726EFF"));
        }else if ("settings".equals(type)) {
            navSettings.setTextColor(Color.parseColor("#726EFF"));
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
        navSettings.setOnClickListener(v -> {
            if (!(this instanceof SettingsActivity)) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
            }
        });
    }
}
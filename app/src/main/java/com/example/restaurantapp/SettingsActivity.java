package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends BaseActivity {

    private Switch switchManagerMode;
    private Switch switchNotifications;

    @Override
    protected String getBottomNavType() {
        return "settings";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupBottomNav();

        switchManagerMode = findViewById(R.id.switch_manager_mode);
        switchNotifications = findViewById(R.id.switch_notifications);

        switchManagerMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Navigate to Partner Login
                Intent intent = new Intent(SettingsActivity.this, PartnerLoginActivity.class);
                startActivity(intent);
                switchManagerMode.setChecked(false); // reset switch
            }
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
    }
}

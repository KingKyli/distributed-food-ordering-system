package com.example.restaurantapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
            String ip = prefs.getString(SettingsActivity.KEY_SERVER_IP, "");
            int port = prefs.getInt(SettingsActivity.KEY_SERVER_PORT, 5000);

            if (ip.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("Server Not Configured")
                        .setMessage("Please set the server IP address in Settings before connecting.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            startActivity(new Intent(this, SettingsActivity.class));
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return;
            }

            new Thread(() -> {
                try {
                    ServerConnection.init(ip, port);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Connected to server", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });
    }
}

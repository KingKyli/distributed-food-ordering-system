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
        btnGetStarted.setOnClickListener(v -> connectToServer());
    }

    private void connectToServer() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_SERVER, MODE_PRIVATE);
        String ip = prefs.getString(SettingsActivity.KEY_SERVER_IP, SettingsActivity.DEFAULT_SERVER_IP);
        int port = prefs.getInt(SettingsActivity.KEY_SERVER_PORT, SettingsActivity.DEFAULT_SERVER_PORT);

        if (ip.isEmpty()) {
            showServerConfigDialog();
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
                        Toast.makeText(this, "Connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void showServerConfigDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Server not configured")
                .setMessage("Please configure the server IP address in Settings before connecting.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(WelcomeActivity.this, SettingsActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

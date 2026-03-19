package com.example.restaurantapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends BaseActivity {

    public static final String PREFS_NAME = "server_prefs";
    public static final String KEY_SERVER_IP = "server_ip";
    public static final String KEY_SERVER_PORT = "server_port";

    private Switch switchManagerMode;
    private Switch switchNotifications;
    private EditText etServerIp;
    private EditText etServerPort;

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
        etServerIp = findViewById(R.id.etServerIp);
        etServerPort = findViewById(R.id.etServerPort);
        Button btnSaveServer = findViewById(R.id.btnSaveServer);

        // Load saved server settings
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        etServerIp.setText(prefs.getString(KEY_SERVER_IP, ""));
        etServerPort.setText(String.valueOf(prefs.getInt(KEY_SERVER_PORT, 5000)));

        btnSaveServer.setOnClickListener(v -> {
            String ip = etServerIp.getText().toString().trim();
            String portStr = etServerPort.getText().toString().trim();
            if (ip.isEmpty() || portStr.isEmpty()) {
                Toast.makeText(this, "IP and Port are required", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int port = Integer.parseInt(portStr);
                prefs.edit()
                        .putString(KEY_SERVER_IP, ip)
                        .putInt(KEY_SERVER_PORT, port)
                        .apply();
                ServerConnection.close();
                Toast.makeText(this, "Server settings saved", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show();
            }
        });

        switchManagerMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Intent intent = new Intent(SettingsActivity.this, PartnerLoginActivity.class);
                startActivity(intent);
                switchManagerMode.setChecked(false);
            }
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
    }
}

package com.example.restaurantapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends BaseActivity {

    public static final String PREFS_SERVER = "server_prefs";
    public static final String KEY_SERVER_IP = "server_ip";
    public static final String KEY_SERVER_PORT = "server_port";
    public static final String DEFAULT_SERVER_IP = "";
    public static final int DEFAULT_SERVER_PORT = 5000;

    private Switch switchManagerMode;
    private Switch switchNotifications;
    private EditText editServerIp;
    private EditText editServerPort;

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
        editServerIp = findViewById(R.id.edit_server_ip);
        editServerPort = findViewById(R.id.edit_server_port);
        Button btnSaveServer = findViewById(R.id.btn_save_server);

        SharedPreferences prefs = getSharedPreferences(PREFS_SERVER, MODE_PRIVATE);
        editServerIp.setText(prefs.getString(KEY_SERVER_IP, DEFAULT_SERVER_IP));
        int savedPort = prefs.getInt(KEY_SERVER_PORT, DEFAULT_SERVER_PORT);
        editServerPort.setText(String.valueOf(savedPort));

        btnSaveServer.setOnClickListener(v -> saveServerSettings(prefs));

        switchManagerMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Intent intent = new Intent(SettingsActivity.this, PartnerLoginActivity.class);
                startActivity(intent);
                switchManagerMode.setChecked(false);
            }
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show()
        );
    }

    private void saveServerSettings(SharedPreferences prefs) {
        String ip = editServerIp.getText().toString().trim();
        String portStr = editServerPort.getText().toString().trim();

        if (ip.isEmpty()) {
            Toast.makeText(this, "Please enter a server IP address", Toast.LENGTH_SHORT).show();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port <= 0 || port > 65535) {
                Toast.makeText(this, "Port must be between 1 and 65535", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit()
                .putString(KEY_SERVER_IP, ip)
                .putInt(KEY_SERVER_PORT, port)
                .apply();

        // Reset the existing connection so the next connect uses the new address
        ServerConnection.close();

        Toast.makeText(this, "Server settings saved", Toast.LENGTH_SHORT).show();
    }
}

package com.example.restaurantapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends BaseActivity {

    private static final String PREFS_SERVER = "server_config";
    private static final String KEY_IP       = "server_ip";
    private static final String KEY_PORT     = "server_port";
    private static final int    DEFAULT_PORT = 8765;

    private Switch switchManagerMode;
    private Switch switchNotifications;
    private EditText editServerIp;
    private EditText editServerPort;
    private Button btnSaveServer;
    private TextView tvServerStatus;

    @Override
    protected String getBottomNavType() { return "settings"; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupBottomNav();

        switchManagerMode   = findViewById(R.id.switch_manager_mode);
        switchNotifications = findViewById(R.id.switch_notifications);
        editServerIp        = findViewById(R.id.edit_server_ip);
        editServerPort      = findViewById(R.id.edit_server_port);
        btnSaveServer       = findViewById(R.id.btn_save_server);
        tvServerStatus      = findViewById(R.id.tv_server_status);

        // Pre-fill with persisted or current values
        SharedPreferences prefs = getSharedPreferences(PREFS_SERVER, MODE_PRIVATE);
        String savedIp   = prefs.getString(KEY_IP, null);
        int    savedPort = prefs.getInt(KEY_PORT, DEFAULT_PORT);

        // Prefer live connection value, fall back to saved, fall back to defaults
        String displayIp   = !TextUtils.isEmpty(ServerConnection.getLastSuccessfulIp())
                ? ServerConnection.getLastSuccessfulIp()
                : (!TextUtils.isEmpty(savedIp) ? savedIp : "");
        int    displayPort = ServerConnection.getLastSuccessfulPort() > 0
                ? ServerConnection.getLastSuccessfulPort()
                : savedPort;

        editServerIp.setText(displayIp);
        editServerPort.setText(displayPort > 0 ? String.valueOf(displayPort) : "");

        // Show current connection status
        if (ServerConnection.isReady()) {
            showServerStatus("Connected to " + displayIp + ":" + displayPort, false);
        } else {
            showServerStatus("Not connected", true);
        }

        // Manager Mode switch
        switchManagerMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Intent intent = new Intent(
                        SettingsActivity.this,
                        PartnerSessionStore.hasActiveSession(SettingsActivity.this)
                                ? ManagerConsoleActivity.class
                                : PartnerLoginActivity.class
                );
                String savedStoreJson = PartnerSessionStore.getStoreJson(SettingsActivity.this);
                if (savedStoreJson != null) intent.putExtra("store_json", savedStoreJson);
                startActivity(intent);
                switchManagerMode.setChecked(false);
            }
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"),
                        Toast.LENGTH_SHORT).show());

        // Save + reconnect
        btnSaveServer.setOnClickListener(v -> saveAndReconnect(prefs));
    }

    private void saveAndReconnect(SharedPreferences prefs) {
        String ipRaw   = editServerIp.getText().toString().trim();
        String portRaw = editServerPort.getText().toString().trim();

        if (TextUtils.isEmpty(ipRaw)) {
            editServerIp.setError("Enter a server IP");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(portRaw);
            if (port <= 0 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            editServerPort.setError("Enter a valid port (1-65535)");
            return;
        }

        prefs.edit().putString(KEY_IP, ipRaw).putInt(KEY_PORT, port).apply();

        btnSaveServer.setEnabled(false);
        showServerStatus("Connecting to " + ipRaw + ":" + port + "…", false);

        final String ip = ipRaw;
        final int finalPort = port;
        new Thread(() -> {
            boolean ok = ServerConnection.init(ip, finalPort);
            ActivityUtils.runOnUiThreadIfAlive(this, () -> {
                btnSaveServer.setEnabled(true);
                if (ok) {
                    showServerStatus("✓ Connected to " + ip + ":" + finalPort, false);
                    Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();
                } else {
                    String err = ServerConnection.getLastError();
                    showServerStatus("✗ " + (err != null ? err : "Could not connect"), true);
                }
            });
        }).start();
    }

    private void showServerStatus(String msg, boolean isError) {
        if (tvServerStatus == null) return;
        tvServerStatus.setText(msg);
        tvServerStatus.setTextColor(isError ? 0xFFD32F2F : 0xFF2E7D32);
        tvServerStatus.setVisibility(View.VISIBLE);
    }
}

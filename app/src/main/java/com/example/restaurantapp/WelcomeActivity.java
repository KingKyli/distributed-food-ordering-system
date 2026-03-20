package com.example.restaurantapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.android.material.button.MaterialButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {
    private static final int DEFAULT_SERVER_PORT = 8765;
    private static final String PREFS_SERVER = "server_config";
    private static final String KEY_IP = "server_ip";
    private static final String KEY_PORT = "server_port";
    // 127.0.0.1      = localhost â†’ used with "adb reverse tcp:8765 tcp:8765" (USB cable)
    // 10.0.2.2       = emulator â†’ host machine
    // 10.0.4.30      = PC on current network
    // 10.4.34.139    = PC on current network (alternate adapter)
    // 192.168.56.1   = VirtualBox host-only
    // 172.20.10.x    = iPhone hotspot range
    private static final List<String> SERVER_IP_FALLBACKS = Arrays.asList(
            "127.0.0.1",
            "10.0.2.2",
            "10.0.4.30",
            "10.4.34.139",
            "192.168.56.1",
            "172.20.10.3",
            "192.168.1.1",
            "192.168.0.1"
    );

    private MaterialButton btnCustomerFlow;
    private MaterialButton btnPartnerFlow;
    private MaterialButton btnRetryConnection;
    private ProgressBar progressConnection;
    private TextView tvConnectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnCustomerFlow = findViewById(R.id.btnCustomerFlow);
        btnPartnerFlow = findViewById(R.id.btnPartnerFlow);
        btnRetryConnection = findViewById(R.id.btnRetryConnection);
        progressConnection = findViewById(R.id.progressConnection);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);

        btnCustomerFlow.setOnClickListener(v -> openNext(MainActivity.class));
        btnPartnerFlow.setOnClickListener(v -> openNext(PartnerLoginActivity.class));
        btnRetryConnection.setOnClickListener(v -> attemptConnection());

        updateConnectionUi(false, "Connecting to server...", false, false);
        attemptConnection();
    }

    private void attemptConnection() {
        updateConnectionUi(false, "Connecting to server...", true, false);
        new Thread(() -> {
            // Build ordered list: saved IP first, then fallbacks
            List<String> ipsToTry = new ArrayList<>();
            SharedPreferences prefs = getSharedPreferences(PREFS_SERVER, MODE_PRIVATE);
            String savedIp = prefs.getString(KEY_IP, null);
            int savedPort = prefs.getInt(KEY_PORT, DEFAULT_SERVER_PORT);
            if (!TextUtils.isEmpty(savedIp)) {
                ipsToTry.add(savedIp.trim());
            }
            for (String ip : SERVER_IP_FALLBACKS) {
                if (!ipsToTry.contains(ip)) ipsToTry.add(ip);
            }

            boolean connected = false;
            String successfulIp = null;
            int usedPort = DEFAULT_SERVER_PORT;

            // Try saved IP with saved port first
            if (!TextUtils.isEmpty(savedIp)) {
                connected = ServerConnection.init(savedIp.trim(), savedPort);
                if (connected) {
                    successfulIp = savedIp.trim();
                    usedPort = savedPort;
                }
            }

            // Try fallbacks with default port
            if (!connected) {
                for (String ip : SERVER_IP_FALLBACKS) {
                    connected = ServerConnection.init(ip, DEFAULT_SERVER_PORT);
                    if (connected) {
                        successfulIp = ip;
                        usedPort = DEFAULT_SERVER_PORT;
                        break;
                    }
                }
            }

            final boolean finalConnected = connected;
            final String finalSuccessfulIp = successfulIp;
            final int finalPort = usedPort;

            ActivityUtils.runOnUiThreadIfAlive(this, () -> {
                if (finalConnected) {
                    updateConnectionUi(true,
                            "âœ“ Connected to " + finalSuccessfulIp + ":" + finalPort + "\nChoose how to continue.",
                            false, false);
                } else {
                    updateConnectionUi(false,
                            "Could not connect to server on port " + DEFAULT_SERVER_PORT + ".\n\n" +
                            "1. Make sure MockServer is running on your PC:\n" +
                            "   java MockServer\n\n" +
                            "2. For physical device, go to:\n" +
                            "   Settings â†’ Server Configuration\n" +
                            "   and enter your PC's IP:\n" +
                            "   â€¢ 10.0.4.30\n" +
                            "   â€¢ 10.4.34.139\n\n" +
                            "3. Make sure phone & PC are on the same WiFi.",
                            false, true);
                }
            });
        }).start();
    }

    private void updateConnectionUi(boolean connected, String status, boolean loading, boolean showRetry) {
        tvConnectionStatus.setText(status);
        progressConnection.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        btnRetryConnection.setVisibility(showRetry ? android.view.View.VISIBLE : android.view.View.GONE);
        btnCustomerFlow.setEnabled(connected && !loading);
        btnPartnerFlow.setEnabled(connected && !loading);
    }

    private void openNext(Class<?> destination) {

        if (!ServerConnection.isReady()) {
            updateConnectionUi(false, "Please connect to the server before continuing.", false, true);
            return;
        }

        Intent intent = new Intent(WelcomeActivity.this, destination);
        if (destination == MainActivity.class) {
            intent.putExtra("RESET_FILTERS", true);
        }
        startActivity(intent);
    }
}


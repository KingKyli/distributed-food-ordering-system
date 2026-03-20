package com.example.restaurantapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
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
    private static final List<String> SERVER_IP_FALLBACKS = Arrays.asList(
            "10.0.2.2",
            "172.20.10.3",
            "192.168.56.1",
            "192.168.1.1",
            "192.168.0.1"
    );

    private Button btnCustomerFlow;
    private Button btnPartnerFlow;
    private Button btnRetryConnection;
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
                            "✓ Connected to " + finalSuccessfulIp + ":" + finalPort + "\nChoose how to continue.",
                            false, false);
                } else {
                    updateConnectionUi(false,
                            "Could not connect to server on port " + DEFAULT_SERVER_PORT + ".\n\n" +
                            "Make sure the mock server is running on your PC:\n" +
                            "  java MockServer\n\n" +
                            "Emulator address: 10.0.2.2:" + DEFAULT_SERVER_PORT + "\n" +
                            "Physical device: set your PC's IP in Settings.",
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

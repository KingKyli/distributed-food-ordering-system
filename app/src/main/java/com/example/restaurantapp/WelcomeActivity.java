package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {
    private static final int DEFAULT_SERVER_PORT = 8765;
    private static final List<String> SERVER_IP_FALLBACKS = Arrays.asList(
            "10.0.2.2",
            "172.20.10.3",
            "192.168.56.1"
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
            boolean connected = false;
            String successfulIp = null;

            for (String ip : SERVER_IP_FALLBACKS) {
                connected = ServerConnection.init(ip, DEFAULT_SERVER_PORT);
                if (connected) {
                    successfulIp = ip;
                    break;
                }
            }

            final boolean finalConnected = connected;
            final String finalSuccessfulIp = successfulIp;

            ActivityUtils.runOnUiThreadIfAlive(this, () -> {
                if (finalConnected) {
                    updateConnectionUi(true, "Connected to " + finalSuccessfulIp + ". Choose how you want to continue.", false, false);
                } else {
                    String message = ServerConnection.getLastError();
                    if (message == null || message.trim().isEmpty()) {
                        message = "Connection failed on all known server addresses. For the emulator, make sure a server is running on your PC at 10.0.2.2:" + DEFAULT_SERVER_PORT + ".";
                    } else {
                        message = message
                                + "\n\nNo server appears to be listening on port " + DEFAULT_SERVER_PORT + "."
                                + "\nIf you use the local mock server, start it on your PC first."
                                + "\nEmulator host: 10.0.2.2:" + DEFAULT_SERVER_PORT
                                + "\nTried: " + SERVER_IP_FALLBACKS;
                    }
                    updateConnectionUi(false, message, false, true);
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
        if (destination == PartnerLoginActivity.class && PartnerSessionStore.hasActiveSession(this)) {
            Intent managerIntent = new Intent(WelcomeActivity.this, ManagerConsoleActivity.class);
            String savedStoreJson = PartnerSessionStore.getStoreJson(this);
            if (savedStoreJson != null) {
                managerIntent.putExtra("store_json", savedStoreJson);
            }
            startActivity(managerIntent);
            return;
        }

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

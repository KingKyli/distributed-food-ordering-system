package com.example.restaurantapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

import java.security.SecureRandom;
import java.util.List;

public class PartnerLoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvStatus;
    private List<Store> stores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvStatus = findViewById(R.id.tvStatus);

        btnLogin.setEnabled(false);
        showStatus("Loading stores...", 0xFF666666);

        MasterCommunicator comm = ConnectionUtils.requireConnected(this);
        if (comm == null) {
            return;
        }

        // ΑΛΛΑΓΗ: Στείλε κενές τιμές για να πάρεις όλα τα καταστήματα
        new Thread(() -> {
            try {
                String result = comm.sendSearchRequest("", "", "", "", "");
                if (result == null || result.trim().isEmpty()) {
                    runOnUiThread(() -> {
                        showStatus("Failed to load stores", 0xFFFF0000);
                        btnLogin.setEnabled(false);
                    });
                    return;
                }
                List<Store> parsedList = MainActivity.parseStores(result);
                runOnUiThread(() -> {
                    stores = parsedList;
                    btnLogin.setEnabled(true);
                    tvStatus.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showStatus("Failed to load stores", 0xFFFF0000);
                    btnLogin.setEnabled(false);
                });
            }
        }).start();

        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvStatus.setVisibility(View.GONE);
            }
            public void afterTextChanged(Editable s) {}
        };
        etUsername.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);

        btnLogin.setOnClickListener(view -> {
            if (stores == null) {
                showStatus("Stores are still loading", 0xFFFF0000);
                return;
            }
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                tvStatus.setTextColor(0xFFFF0000);
                tvStatus.setText("Please fill in all fields");
                tvStatus.setVisibility(View.VISIBLE);
                return;
            }
            Store matchedStore = findStoreByName(username);
            if (matchedStore == null) {
                showStatus("Store not found", 0xFFFF0000);
                return;
            }

            String expectedPassword = PartnerLoginManager.getInstance().getOrGeneratePassword(username, this);
            if (password.equals(expectedPassword)) {
                tvStatus.setTextColor(0xFF00AA00);
                tvStatus.setText("Login successful");
                tvStatus.setVisibility(View.VISIBLE);
                // Open ManagerConsoleActivity with store JSON
                android.content.Intent intent = new android.content.Intent(PartnerLoginActivity.this, ManagerConsoleActivity.class);
                try {
                    intent.putExtra("store_json", matchedStore.toJson().toString());
                } catch (Exception e) {
                    showStatus("Error passing store data", 0xFFFF0000);
                    return;
                }
                startActivity(intent);
                finish();
                return;
            } else {
                tvStatus.setTextColor(0xFFFF0000);
                tvStatus.setText("Invalid credentials");
            }
            tvStatus.setVisibility(View.VISIBLE);
        });
    }
    private Store findStoreByName(String name) {
        if (stores == null) return null;
        for (Store s : stores) {
            if (s.getStoreName().equalsIgnoreCase(name)) return s;
        }
        return null;
    }

    private void showStatus(String message, int color) {
        tvStatus.setTextColor(color);
        tvStatus.setText(message);
        tvStatus.setVisibility(View.VISIBLE);
    }
}


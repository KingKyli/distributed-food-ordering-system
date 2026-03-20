package com.example.restaurantapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class PartnerLoginActivity extends AppCompatActivity {

    private Spinner spinnerStores;
    private EditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnRequestAccessCode;
    private MaterialButton btnRetryStores;
    private ProgressBar progressStores;
    private TextView tvStatus;
    private TextView tvPasswordHint;
    private MaterialCardView cardActiveSession;
    private TextView tvActiveSessionStoreName;
    private MaterialButton btnContinueSession;
    private MaterialButton btnSwitchStore;
    private List<Store> stores;
    private String requestedAccessCodeStoreName;
    private volatile boolean activityActive;
    private final PartnerAuthService partnerAuthService = new PartnerAuthService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ActivityUtils.ensureConnectedOrRedirect(this)) {
            return;
        }

        setContentView(R.layout.activity_partner_login);
        activityActive = true;

        spinnerStores          = findViewById(R.id.spinnerStores);
        etUsername             = findViewById(R.id.etUsername);
        etPassword             = findViewById(R.id.etPassword);
        btnLogin               = findViewById(R.id.btnLogin);
        btnRequestAccessCode   = findViewById(R.id.btnRequestAccessCode);
        btnRetryStores         = findViewById(R.id.btnRetryStores);
        progressStores         = findViewById(R.id.progressStores);
        tvStatus               = findViewById(R.id.tvStatus);
        tvPasswordHint         = findViewById(R.id.tvPasswordHint);
        cardActiveSession      = findViewById(R.id.cardActiveSession);
        tvActiveSessionStoreName = findViewById(R.id.tvActiveSessionStoreName);
        btnContinueSession     = findViewById(R.id.btnContinueSession);
        btnSwitchStore         = findViewById(R.id.btnSwitchStore);

        // Show active-session card if a session already exists
        boolean forceLogin = getIntent().getBooleanExtra("FORCE_LOGIN", false);
        if (!forceLogin && PartnerSessionStore.hasActiveSession(this)) {
            String savedStoreName = PartnerSessionStore.getStoreName(this);
            if (savedStoreName != null && !savedStoreName.isEmpty()) {
                cardActiveSession.setVisibility(View.VISIBLE);
                tvActiveSessionStoreName.setText(savedStoreName);
            }
        }

        btnContinueSession.setOnClickListener(v -> openManagerConsoleFromSession());

        btnSwitchStore.setOnClickListener(v -> {
            PartnerSessionStore.clear(this);
            cardActiveSession.setVisibility(View.GONE);
            tvStatus.setVisibility(View.GONE);
        });

        btnRetryStores.setOnClickListener(v -> loadStores());
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateActionButtons(false);
                // Only hide status if it's NOT the success message from access code request
                if (tvStatus.getVisibility() == View.VISIBLE) {
                    String current = tvStatus.getText() != null ? tvStatus.getText().toString() : "";
                    if (!current.contains("Access code")) {
                        tvStatus.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        spinnerStores.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                requestedAccessCodeStoreName = null;
                if (position <= 0) {
                    etUsername.setText("");
                    etPassword.setText("");
                } else {
                    String selectedStore = (String) parent.getItemAtPosition(position);
                    etUsername.setText(selectedStore);
                    etPassword.setText("");
                }
                updateActionButtons(false);
                tvStatus.setVisibility(View.GONE);
                tvPasswordHint.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                requestedAccessCodeStoreName = null;
                etUsername.setText("");
                etPassword.setText("");
                updateActionButtons(false);
            }
        });

        btnRequestAccessCode.setOnClickListener(v -> {
            String storeName = getSelectedStoreName();
            if (storeName.isEmpty()) {
                showStatus("Select your store first.", 0xFFD32F2F);
                return;
            }
            requestedAccessCodeStoreName = null;
            setLoadingState(true, "Sending access code...");
            new Thread(() -> {
                AppResult<PartnerAccessCodeInfo> result = partnerAuthService.requestAccessCode(storeName);
                ActivityUtils.runOnUiThreadIfAlive(this, () -> {
                    if (!activityActive) {
                        return;
                    }
                    setLoadingState(false, null);
                    if (!result.isSuccess()) {
                        showStatus(result.getMessage(), 0xFFD32F2F);
                        tvPasswordHint.setVisibility(View.GONE);
                        updateActionButtons(false);
                        return;
                    }

                    PartnerAccessCodeInfo info = result.getData();
                    requestedAccessCodeStoreName = storeName;
                    showStatus("Access code sent successfully", 0xFF2E7D32);
                    etPassword.setText(info.getDemoCode());
                    etPassword.setSelection(etPassword.getText() != null ? etPassword.getText().length() : 0);
                    tvPasswordHint.setText(getString(
                            R.string.partner_access_code_hint,
                            info.getDeliveryDestination(),
                            info.getDemoCode(),
                            info.getExpiresInMinutes()
                    ));
                    tvPasswordHint.setVisibility(View.VISIBLE);
                    etPassword.requestFocus();
                    updateActionButtons(false);
                });
            }).start();
        });

        setLoadingState(true, "Loading stores...");
        loadStores();

        btnLogin.setOnClickListener(view -> {
            String username = getSelectedStoreName();
            String password = etPassword.getText().toString().trim();

            setLoadingState(true, "Signing in...");
            new Thread(() -> {
                AppResult<Store> result = partnerAuthService.loginPartner(username, password);
                ActivityUtils.runOnUiThreadIfAlive(this, () -> {
                    if (!activityActive) {
                        return;
                    }
                    setLoadingState(false, null);
                    if (!result.isSuccess()) {
                        showStatus(result.getMessage(), 0xFFD32F2F);
                        return;
                    }

                    showStatus("Login successful", 0xFF2E7D32);
                    requestedAccessCodeStoreName = null;
                    PartnerSessionStore.saveSession(PartnerLoginActivity.this, result.getData());
                    android.content.Intent intent = new android.content.Intent(PartnerLoginActivity.this, ManagerConsoleActivity.class);
                    try {
                        intent.putExtra("store_json", result.getData().toJson().toString());
                    } catch (Exception e) {
                        showStatus("Error passing store data", 0xFFD32F2F);
                        return;
                    }
                    startActivity(intent);
                    finish();
                });
            }).start();
        });
    }

    @Override
    protected void onDestroy() {
        activityActive = false;
        super.onDestroy();
    }

    private void loadStores() {
        setLoadingState(true, "Loading stores...");
        new Thread(() -> {
            AppResult<List<Store>> result = partnerAuthService.loadStores();
            ActivityUtils.runOnUiThreadIfAlive(this, () -> {
                if (!activityActive) {
                    return;
                }
                if (!result.isSuccess()) {
                    setErrorState(result.getMessage());
                    return;
                }

                stores = result.getData();
                setupStoreSpinner(stores);
                setLoadingState(false, stores.isEmpty() ? "No partner stores found." : null);
                updateActionButtons(false);
            });
        }).start();
    }

    private void openManagerConsoleFromSession() {
        String savedStoreJson = PartnerSessionStore.getStoreJson(this);
        if (savedStoreJson == null || savedStoreJson.trim().isEmpty()) {
            PartnerSessionStore.clear(this);
            return;
        }
        android.content.Intent intent = new android.content.Intent(this, ManagerConsoleActivity.class);
        intent.putExtra("store_json", savedStoreJson);
        startActivity(intent);
        finish();
    }

    private void setupStoreSpinner(List<Store> parsedList) {
        List<String> storeNames = new ArrayList<>();
        storeNames.add("-- Select your store --");
        if (parsedList != null) {
            for (Store store : parsedList) {
                if (store != null && store.getStoreName() != null) {
                    storeNames.add(store.getStoreName());
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, storeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStores.setAdapter(adapter);
        spinnerStores.setEnabled(true);
        spinnerStores.setSelection(0);
    }

    private String getSelectedStoreName() {
        Object selectedItem = spinnerStores.getSelectedItem();
        if (selectedItem instanceof String && spinnerStores.getSelectedItemPosition() > 0) {
            return ((String) selectedItem).trim();
        }
        return etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
    }

    private boolean hasPasswordInput() {
        return etPassword.getText() != null && !etPassword.getText().toString().trim().isEmpty();
    }

    private boolean hasRequestedAccessCodeForSelectedStore() {
        String selectedStoreName = getSelectedStoreName();
        return !selectedStoreName.isEmpty()
                && requestedAccessCodeStoreName != null
                && requestedAccessCodeStoreName.equalsIgnoreCase(selectedStoreName);
    }

    private void updateActionButtons(boolean loading) {
        boolean hasStoreSelection = spinnerStores.getSelectedItemPosition() > 0;
        boolean codeRequested = hasRequestedAccessCodeForSelectedStore();
        boolean hasPassword = hasPasswordInput();

        spinnerStores.setEnabled(!loading);
        etPassword.setEnabled(!loading && hasStoreSelection);
        btnRequestAccessCode.setEnabled(!loading && hasStoreSelection);
        btnLogin.setEnabled(!loading && hasStoreSelection && codeRequested && hasPassword);

        // Show a subtle step hint when store selected but no code yet
        if (!loading && hasStoreSelection && !codeRequested && tvStatus.getVisibility() != View.VISIBLE) {
            showStatus("Step 1: Tap \"Send Access Code\" to receive your login code.", 0xFF1565C0);
        }
    }

    private void showStatus(String message, int color) {
        tvStatus.setTextColor(color);
        tvStatus.setText(message);
        tvStatus.setVisibility(View.VISIBLE);
    }

    private void setLoadingState(boolean loading, String message) {
        progressStores.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRetryStores.setVisibility(View.GONE);
        updateActionButtons(loading);
        if (message == null || message.isEmpty()) {
            tvStatus.setVisibility(View.GONE);
        } else {
            showStatus(message, 0xFF757575);
        }
    }

    private void setErrorState(String message) {
        progressStores.setVisibility(View.GONE);
        btnRetryStores.setVisibility(View.VISIBLE);
        updateActionButtons(false);
        showStatus(message, 0xFFD32F2F);
    }
}



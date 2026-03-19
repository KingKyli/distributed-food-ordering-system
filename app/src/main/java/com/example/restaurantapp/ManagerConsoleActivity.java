package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;
import java.util.List;

public class ManagerConsoleActivity extends AppCompatActivity {

    private TextView tvTotalProducts, tvLowStock, tvOutOfStock, tvStoreName;
    private String currentStoreJson;
    private Store currentStore;
    private volatile boolean activityActive;
    private final RestaurantRepository restaurantRepository = new RestaurantRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ActivityUtils.ensureConnectedOrRedirect(this)) {
            return;
        }

        setContentView(R.layout.activity_manager_console);
        activityActive = true;

        MaterialCardView btnAddProduct = findViewById(R.id.btnAddProduct);
        MaterialCardView btnEditProduct = findViewById(R.id.btnEditProduct);
        MaterialButton btnSwitchStore = findViewById(R.id.btnSwitchStore);

        // Bind views
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvLowStock = findViewById(R.id.tvLowStock);
        tvOutOfStock = findViewById(R.id.tvOutOfStock);
        tvStoreName = findViewById(R.id.tvStoreName);

        currentStoreJson = resolveStoreJson();
        android.util.Log.d("ManagerConsoleActivity", "Your Store is: " + currentStoreJson);
        if (currentStoreJson != null) {
            try {
                JSONObject jsonObj = new JSONObject(currentStoreJson);
                currentStore = Store.fromJson(jsonObj);
                PartnerSessionStore.saveSession(this, currentStore);
                if (tvStoreName != null) {
                    tvStoreName.setText(currentStore.getStoreName());
                }
                updateInventorySummary(currentStore);
            } catch (Exception e) {
                android.util.Log.e("ManagerConsoleActivity", "Failed to restore store data", e);
                PartnerSessionStore.clear(this);
                redirectToPartnerLogin();
                return;
            }
        } else {
            redirectToPartnerLogin();
            return;
        }

        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerConsoleActivity.this, AddProductActivity.class);
            intent.putExtra("store_json", currentStoreJson);
            startActivity(intent);
        });

        btnEditProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerConsoleActivity.this, EditProductActivity.class);
            intent.putExtra("store_json", currentStoreJson);
            startActivity(intent);
        });

        btnSwitchStore.setOnClickListener(v -> {
            PartnerSessionStore.clear(this);
            Intent intent = new Intent(ManagerConsoleActivity.this, PartnerLoginActivity.class);
            intent.putExtra("FORCE_LOGIN", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStoreData();
    }

    @Override
    protected void onDestroy() {
        activityActive = false;
        super.onDestroy();
    }

    private void refreshStoreData() {
        final String storeJson = currentStoreJson != null ? currentStoreJson : resolveStoreJson();
        if (storeJson == null) return;
        final String storeName = StoreJsonUtils.extractStoreName(storeJson);
        if (storeName == null || storeName.trim().isEmpty()) return;
        new Thread(() -> {
            AppResult<Store> result = restaurantRepository.fetchStoreByName(storeName);
            ActivityUtils.runOnUiThreadIfAlive(this, () -> {
                if (!activityActive) {
                    return;
                }
                if (!result.isSuccess()) {
                    if (tvStoreName != null) {
                        tvStoreName.setText(result.getMessage());
                    }
                    return;
                }

                Store store = result.getData();
                currentStore = store;
                try {
                    currentStoreJson = store.toJson() != null ? store.toJson().toString() : currentStoreJson;
                } catch (Exception ignored) {
                }
                PartnerSessionStore.saveSession(ManagerConsoleActivity.this, store);
                if (tvStoreName != null) {
                    tvStoreName.setText(store.getStoreName());
                }
                updateInventorySummary(store);
            });
        }).start();
    }

    private void updateInventorySummary(Store store) {
        if (store == null) {
            return;
        }
        List<Product> products = store.getProducts();

        int total = products.size();
        int lowStock = 0;
        int outOfStock = 0;

        for (Product p : products) {
            int available = p.getAvailableAmount();

            if (available == 0) {
                outOfStock++;
            } else if (available <= 2) {
                lowStock++;
            }
        }

        tvTotalProducts.setText("📦 Total Products: " + total);
        tvLowStock.setText("⚠️ Low Stock: " + lowStock);
        tvOutOfStock.setText("💲 Out of Stock: " + outOfStock);
    }

    private String resolveStoreJson() {
        String fromIntent = getIntent().getStringExtra("store_json");
        if (fromIntent != null && !fromIntent.trim().isEmpty()) {
            return fromIntent;
        }
        return PartnerSessionStore.getStoreJson(this);
    }

    private void redirectToPartnerLogin() {
        Intent intent = new Intent(this, PartnerLoginActivity.class);
        intent.putExtra("FORCE_LOGIN", true);
        startActivity(intent);
        finish();
    }

}

package com.example.restaurantapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;
import java.util.List;
import java.util.Locale;

public class ManagerConsoleActivity extends AppCompatActivity {

    private TextView tvTotalProducts, tvInventoryValue, tvLowStock, tvOutOfStock;
    private TextView tvFooterTotal, tvFooterInventory, tvFooterLowStock, tvFooterOutOfStock;
    private TextView tvStoreName;
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

        MaterialCardView btnAddProduct  = findViewById(R.id.btnAddProduct);
        MaterialCardView btnEditProduct = findViewById(R.id.btnEditProduct);
        MaterialButton   btnSwitchStore = findViewById(R.id.btnSwitchStore);
        tvStoreName = findViewById(R.id.tvStoreName);

        // Bind the 4 stat cards
        android.view.View cardTotal     = findViewById(R.id.statTotalProducts);
        android.view.View cardInventory = findViewById(R.id.statInventoryValue);
        android.view.View cardLowStock  = findViewById(R.id.statLowStock);
        android.view.View cardOutOfStock= findViewById(R.id.statOutOfStock);

        tvTotalProducts  = cardTotal.findViewById(R.id.tvValue);
        tvInventoryValue = cardInventory.findViewById(R.id.tvValue);
        tvLowStock       = cardLowStock.findViewById(R.id.tvValue);
        tvOutOfStock     = cardOutOfStock.findViewById(R.id.tvValue);

        tvFooterTotal     = cardTotal.findViewById(R.id.tvFooterValue);
        tvFooterInventory = cardInventory.findViewById(R.id.tvFooterValue);
        tvFooterLowStock  = cardLowStock.findViewById(R.id.tvFooterValue);
        tvFooterOutOfStock= cardOutOfStock.findViewById(R.id.tvFooterValue);

        // Configure each card: accent stroke + icon circle color + icon emoji + label
        configureStatCard(cardTotal,      R.color.stat_orange, R.color.stat_orange_bg, "\uD83D\uDECD\uFE0F", "Total Products");
        configureStatCard(cardInventory,  R.color.stat_orange, R.color.stat_orange_bg, "\uD83D\uDCB0",       "Revenue");
        configureStatCard(cardLowStock,   R.color.stat_gold,   R.color.stat_gold_bg,   "\u23F0",             "Low Stock");
        configureStatCard(cardOutOfStock, R.color.stat_purple, R.color.stat_purple_bg, "\uD83E\uDD0D",       "Out of Stock");

        currentStoreJson = resolveStoreJson();
        if (currentStoreJson != null) {
            try {
                JSONObject jsonObj = new JSONObject(currentStoreJson);
                currentStore = Store.fromJson(jsonObj);
                PartnerSessionStore.saveSession(this, currentStore);
                tvStoreName.setText(currentStore.getStoreName());
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
            Intent intent = new Intent(this, AddProductActivity.class);
            intent.putExtra("store_json", currentStoreJson);
            startActivity(intent);
        });

        btnEditProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProductActivity.class);
            intent.putExtra("store_json", currentStoreJson);
            startActivity(intent);
        });

        btnSwitchStore.setOnClickListener(v -> {
            PartnerSessionStore.clear(this);
            Intent intent = new Intent(this, PartnerLoginActivity.class);
            intent.putExtra("FORCE_LOGIN", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    /** Applies accent stroke colour, icon-circle tint, emoji, and label to a stat card. */
    private void configureStatCard(android.view.View card, int strokeColorRes, int circleBgRes,
                                   String icon, String label) {
        if (card instanceof MaterialCardView) {
            ((MaterialCardView) card).setStrokeColor(ContextCompat.getColor(this, strokeColorRes));
        }
        FrameLayout iconWrap = card.findViewById(R.id.iconWrap);
        if (iconWrap != null) {
            iconWrap.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, circleBgRes)));
        }
        ((TextView) card.findViewById(R.id.tvIcon)).setText(icon);
        ((TextView) card.findViewById(R.id.tvLabel)).setText(label);
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
                if (!activityActive) return;
                if (!result.isSuccess()) {
                    tvStoreName.setText(result.getMessage());
                    return;
                }
                Store store = result.getData();
                currentStore = store;
                try {
                    currentStoreJson = store.toJson() != null
                            ? store.toJson().toString() : currentStoreJson;
                } catch (Exception ignored) {}
                PartnerSessionStore.saveSession(this, store);
                tvStoreName.setText(store.getStoreName());
                updateInventorySummary(store);
            });
        }).start();
    }

    private void updateInventorySummary(Store store) {
        if (store == null) return;
        List<Product> products = store.getProducts();

        int total      = products.size();
        int lowStock   = 0;
        int outOfStock = 0;
        double totalValue     = 0;
        double lowStockValue  = 0;
        double outOfStockValue= 0;

        for (Product p : products) {
            int qty = p.getAvailableAmount();
            double itemValue = qty * p.getPrice();
            totalValue += itemValue;
            if (qty == 0) {
                outOfStock++;
                outOfStockValue += p.getPrice(); // potential per-unit lost
            } else if (qty <= 2) {
                lowStock++;
                lowStockValue += itemValue;
            }
        }

        double avgPrice = total > 0 ? totalValue / total : 0;

        // Card 1 – Total Products
        tvTotalProducts.setText(String.valueOf(total));
        tvFooterTotal.setText(String.format(Locale.getDefault(), "avg \u20AC%.2f", avgPrice));

        // Card 2 – Inventory / Revenue value
        tvInventoryValue.setText(formatCurrency(totalValue));
        tvFooterInventory.setText(formatCurrency(totalValue));

        // Card 3 – Low Stock
        tvLowStock.setText(String.valueOf(lowStock));
        tvFooterLowStock.setText(String.format(Locale.getDefault(), "\u20AC%.2f at risk", lowStockValue));

        // Card 4 – Out of Stock
        tvOutOfStock.setText(String.valueOf(outOfStock));
        tvFooterOutOfStock.setText(String.format(Locale.getDefault(), "\u20AC%.2f lost", outOfStockValue));
    }

    private String formatCurrency(double value) {
        if (value >= 1000) {
            return String.format(Locale.getDefault(), "\u20AC%,.0f", value);
        }
        return String.format(Locale.getDefault(), "\u20AC%.2f", value);
    }

    private String resolveStoreJson() {
        String fromIntent = getIntent().getStringExtra("store_json");
        if (fromIntent != null && !fromIntent.trim().isEmpty()) return fromIntent;
        return PartnerSessionStore.getStoreJson(this);
    }

    private void redirectToPartnerLogin() {
        Intent intent = new Intent(this, PartnerLoginActivity.class);
        intent.putExtra("FORCE_LOGIN", true);
        startActivity(intent);
        finish();
    }
}

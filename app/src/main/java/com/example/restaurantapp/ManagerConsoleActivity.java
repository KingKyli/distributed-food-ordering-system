package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.util.List;

public class ManagerConsoleActivity extends AppCompatActivity {

    private TextView tvTotalProducts, tvLowStock, tvOutOfStock, tvStoreName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_console);

        LinearLayout btnAddProduct = findViewById(R.id.btnAddProduct);
        LinearLayout btnEditProduct = findViewById(R.id.btnEditProduct);

        // Bind views
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvLowStock = findViewById(R.id.tvLowStock);
        tvOutOfStock = findViewById(R.id.tvOutOfStock);
        tvStoreName = findViewById(R.id.tvStoreName);

        // Get the store JSON string (passed from previous screen or hardcoded for testing)
        String storeJson = getIntent().getStringExtra("store_json");
        android.util.Log.d("ManagerConsoleActivity", "Your Store is: " + storeJson);
        if (storeJson != null) {
            try {
                JSONObject jsonObj = new JSONObject(storeJson);
                Store store = Store.fromJson(jsonObj);
                if (tvStoreName != null) {
                    tvStoreName.setText(store.getStoreName());
                }
                updateInventorySummary(store);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerConsoleActivity.this, AddProductActivity.class);
            intent.putExtra("store_json", storeJson);  // 🔁 Pass the same JSON string
            startActivity(intent);
        });

        btnEditProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerConsoleActivity.this, EditProductActivity.class);
            intent.putExtra("store_json", storeJson);  // 🔁 Pass the same JSON string
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStoreData();
    }

    private void refreshStoreData() {
        final String storeJson = getIntent().getStringExtra("store_json");
        if (storeJson == null) return;
        final String storeName;
        try {
            JSONObject jsonObj = new JSONObject(storeJson);
            storeName = jsonObj.getString("StoreName");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (storeName == null) return;
        new Thread(() -> {
            MasterCommunicator comm = ConnectionUtils.requireConnected(this);
            if (comm == null) return;
            // Στείλε ΚΕΝΕΣ τιμές για να πάρεις ΟΛΑ τα καταστήματα
            String result = comm.sendSearchRequest("", "", "", "", "");
            runOnUiThread(() -> {
                try {
                    org.json.JSONArray arr = new org.json.JSONArray(result);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        if (obj.getString("StoreName").equalsIgnoreCase(storeName)) {
                            Store store = Store.fromJson(obj);
                            if (tvStoreName != null) tvStoreName.setText(store.getStoreName());
                            updateInventorySummary(store);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    private void updateInventorySummary(Store store) {
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

}

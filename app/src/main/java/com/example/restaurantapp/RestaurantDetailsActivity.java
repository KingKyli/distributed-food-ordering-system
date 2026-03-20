package com.example.restaurantapp;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Shows the details and product list for a single restaurant.
 * <p>
 * The Activity receives the store as a JSON string via the {@code store_json} Intent extra.
 * It immediately parses that string into a typed {@link Store} model (once, at entry), then
 * works exclusively with {@link Store} and {@link Product} objects — no further raw-JSON
 * handling occurs inside this class or its adapters.
 */
public class RestaurantDetailsActivity extends AppCompatActivity {

    TextView tvStoreName, tvCategory, tvStars, tvPrice;
    TextView tvDetailsStatus;
    RecyclerView rvProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        tvStoreName    = findViewById(R.id.tvStoreName);
        tvCategory     = findViewById(R.id.tvCategory);
        tvStars        = findViewById(R.id.tvStars);
        tvPrice        = findViewById(R.id.tvPrice);
        tvDetailsStatus = findViewById(R.id.tvDetailsStatus);
        rvProducts     = findViewById(R.id.rvProducts);

        String storeJsonStr = getIntent().getStringExtra("store_json");

        if (storeJsonStr == null || storeJsonStr.trim().isEmpty()) {
            showStatus("Store details are not available.");
            return;
        }

        try {
            // Single parse point: JSON → typed Store (which internally builds List<Product>)
            Store store = Store.fromJson(new JSONObject(storeJsonStr));
            bindStore(store);
        } catch (JSONException e) {
            showStatus("Error loading store details.");
            Toast.makeText(this, "Error loading store details", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindStore(Store store) {
        tvStoreName.setText(store.getStoreName());
        tvCategory.setText("Category: " + store.getFoodCategory());

        int stars = store.getStoreStars() != null ? store.getStoreStars() : 0;
        tvStars.setText("\u2b50".repeat(Math.max(1, stars)));

        tvPrice.setText("Price Category: " + store.getPriceCategory());

        List<Product> products = store.getProducts();
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(new ProductAdapter(products, store.getStoreName()));

        if (products.isEmpty()) {
            showStatus("This store has no products available right now.");
        } else {
            tvDetailsStatus.setVisibility(android.view.View.GONE);
        }
    }

    private void showStatus(String message) {
        tvDetailsStatus.setText(message);
        tvDetailsStatus.setVisibility(android.view.View.VISIBLE);
        rvProducts.setVisibility(android.view.View.GONE);
    }
}

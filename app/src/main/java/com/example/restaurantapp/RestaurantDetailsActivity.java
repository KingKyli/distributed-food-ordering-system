package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RestaurantDetailsActivity extends AppCompatActivity {

    private TextView tvStoreName, tvCategory, tvStars, tvPrice, tvDetailsStatus;
    private RecyclerView rvProducts;

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

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        FloatingActionButton fabBasket = findViewById(R.id.fabBasket);
        if (fabBasket != null) {
            fabBasket.setOnClickListener(v -> {
                if (Basket.getInstance().isEmpty()) {
                    Toast.makeText(this, "Your basket is empty", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, BasketActivity.class));
                }
            });
        }

        String storeJsonStr = getIntent().getStringExtra("store_json");
        if (storeJsonStr == null || storeJsonStr.trim().isEmpty()) {
            showStatus("Store details are not available.");
            return;
        }

        try {
            JSONObject storeJson = new JSONObject(storeJsonStr);
            String storeName = storeJson.optString("StoreName", "Store");

            tvStoreName.setText(storeName);
            tvCategory.setText(storeJson.optString("FoodCategory", ""));

            int stars = storeJson.optInt("Stars", 0);
            tvStars.setText("⭐".repeat(Math.max(1, stars)));

            // Parse products from JSON → List<Product> (typed model, no raw JSON in UI)
            JSONArray productsArray = storeJson.optJSONArray("Products");
            List<Product> productList = new ArrayList<>();
            double totalPrice = 0;

            if (productsArray != null) {
                for (int i = 0; i < productsArray.length(); i++) {
                    try {
                        Product p = Product.fromJson(productsArray.getJSONObject(i));
                        productList.add(p);
                        totalPrice += p.getPrice();
                    } catch (JSONException ignored) { }
                }
            }

            double avg = productList.isEmpty() ? 0 : totalPrice / productList.size();
            tvPrice.setText(avg <= 5 ? "$" : avg <= 15 ? "$$" : "$$$");

            rvProducts.setLayoutManager(new LinearLayoutManager(this));
            rvProducts.setAdapter(new ProductAdapter(productList, storeName));

            if (productList.isEmpty()) {
                showStatus("This store has no products available right now.");
            } else {
                tvDetailsStatus.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            showStatus("Error loading store details.");
        }
    }

    private void showStatus(String message) {
        tvDetailsStatus.setText(message);
        tvDetailsStatus.setVisibility(View.VISIBLE);
        rvProducts.setVisibility(View.GONE);
    }
}
package com.example.restaurantapp;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.*;

import java.util.ArrayList;
import java.util.List;

public class RestaurantDetailsActivity extends AppCompatActivity {

    TextView tvStoreName, tvCategory, tvStars, tvPrice;
    TextView tvDetailsStatus;
    RecyclerView rvProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        tvStoreName = findViewById(R.id.tvStoreName);
        tvCategory = findViewById(R.id.tvCategory);
        tvStars = findViewById(R.id.tvStars);
        tvPrice = findViewById(R.id.tvPrice);
        tvDetailsStatus = findViewById(R.id.tvDetailsStatus);
        rvProducts = findViewById(R.id.rvProducts);

        String storeJsonStr = getIntent().getStringExtra("store_json");

        if (storeJsonStr == null || storeJsonStr.trim().isEmpty()) {
            showStatus("Store details are not available.");
            return;
        }

        try {
            JSONObject storeJson = new JSONObject(storeJsonStr);

            String currentStoreName = storeJson.getString("StoreName");

            tvStoreName.setText(currentStoreName);
            tvCategory.setText("Category: " + storeJson.getString("FoodCategory"));

            int stars = storeJson.getInt("Stars");
            tvStars.setText("⭐".repeat(Math.max(1, stars)));

            JSONArray productsArray = storeJson.optJSONArray("Products");
            List<JSONObject> productList = new ArrayList<>();
            double totalPrice = 0;

            if (productsArray != null) {
                for (int i = 0; i < productsArray.length(); i++) {
                    JSONObject product = productsArray.getJSONObject(i);
                    productList.add(product);
                    totalPrice += product.optDouble("Price", 0);
                }
            }

            double avgPrice = productList.isEmpty() ? 0 : totalPrice / productList.size();
            String priceCategory = (avgPrice <= 5) ? "$" : (avgPrice <= 15) ? "$$" : "$$$";
            tvPrice.setText("Price Category: " + priceCategory);

            rvProducts.setLayoutManager(new LinearLayoutManager(this));
            rvProducts.setAdapter(new ProductAdapter(productList, currentStoreName));

            if (productList.isEmpty()) {
                showStatus("This store has no products available right now.");
            } else {
                tvDetailsStatus.setVisibility(android.view.View.GONE);
            }
        } catch (JSONException e) {
            showStatus("Error loading store details.");
            Toast.makeText(this, "Error loading store details", Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatus(String message) {
        tvDetailsStatus.setText(message);
        tvDetailsStatus.setVisibility(android.view.View.VISIBLE);
        rvProducts.setVisibility(android.view.View.GONE);
    }
}
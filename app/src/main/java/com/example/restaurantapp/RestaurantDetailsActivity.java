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
    RecyclerView rvProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        tvStoreName = findViewById(R.id.tvStoreName);
        tvCategory = findViewById(R.id.tvCategory);
        tvStars = findViewById(R.id.tvStars);
        tvPrice = findViewById(R.id.tvPrice);
        rvProducts = findViewById(R.id.rvProducts);

        String storeJsonStr = getIntent().getStringExtra("store_json");

        if (storeJsonStr != null) {
            try {
                JSONObject storeJson = new JSONObject(storeJsonStr);

                String currentStoreName = storeJson.getString("StoreName"); // <-- define here

                tvStoreName.setText(currentStoreName);
                tvCategory.setText("Category: " + storeJson.getString("FoodCategory"));

                int stars = storeJson.getInt("Stars");
                tvStars.setText("⭐".repeat(Math.max(1, stars)));

                JSONArray productsArray = storeJson.getJSONArray("Products");
                List<JSONObject> productList = new ArrayList<>();
                double totalPrice = 0;

                for (int i = 0; i < productsArray.length(); i++) {
                    JSONObject product = productsArray.getJSONObject(i);
                    productList.add(product);
                    double price = product.getDouble("Price");
                    totalPrice += price;
                }

                double avgPrice = productsArray.length() > 0 ? totalPrice / productsArray.length() : 0;
                String priceCategory = (avgPrice <= 5) ? "$" : (avgPrice <= 15) ? "$$" : "$$$";
                tvPrice.setText("Price Category: " + priceCategory);

                // Pass currentStoreName to ProductAdapter
                ProductAdapter adapter = new ProductAdapter(productList, currentStoreName);
                rvProducts.setLayoutManager(new LinearLayoutManager(this));
                rvProducts.setAdapter(adapter);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading store details", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
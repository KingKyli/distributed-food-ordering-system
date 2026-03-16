package com.example.restaurantapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import com.example.restaurantapp.Store;
import com.example.restaurantapp.StoreAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends BaseActivity {

    private RecyclerView rvRestaurants;
    private TextView tvNoFilters;
    private List<Store> storeList = new ArrayList<>();

    @Override
    protected String getBottomNavType() {
        return "home";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBottomNav();

        rvRestaurants = findViewById(R.id.rvRestaurants);
        tvNoFilters = findViewById(R.id.tvNoFilters);
        // Java
        SharedPreferences prefs = getSharedPreferences("filters_prefs", MODE_PRIVATE);

        Intent intent = getIntent();
        String cuisine = intent.hasExtra("FILTER_CUISINE") ?
                intent.getStringExtra("FILTER_CUISINE") : prefs.getString("cuisine", "");
        int distance = intent.hasExtra("FILTER_DISTANCE") ?
                intent.getIntExtra("FILTER_DISTANCE", 0) : prefs.getInt("distance", 0);
        String latitude = intent.hasExtra("FILTER_LATITUDE") ?
                intent.getStringExtra("FILTER_LATITUDE") : prefs.getString("latitude", "");
        String longitude = intent.hasExtra("FILTER_LONGITUDE") ?
                intent.getStringExtra("FILTER_LONGITUDE") : prefs.getString("longitude", "");
        int stars = intent.hasExtra("FILTER_STARS") ?
                intent.getIntExtra("FILTER_STARS", 0) : prefs.getInt("stars", 0);
        String price = intent.hasExtra("FILTER_PRICE") ?
                intent.getStringExtra("FILTER_PRICE") : "";
        boolean openNow = intent.hasExtra("FILTER_OPEN_NOW") ?
                intent.getBooleanExtra("FILTER_OPEN_NOW", false) : prefs.getBoolean("openNow", false);

        // Use these variables for your search request
        String starsStr = String.valueOf(stars);
        new Thread(() -> {
            MasterCommunicator comm = ServerConnection.getInstance();
            try {
                String result = comm.sendSearchRequest(latitude, longitude, cuisine, starsStr, price);
                android.util.Log.d("MainActivity", "Server response: " + result);
                List<Store> parsedList = parseStores(result);

                runOnUiThread(() -> {
                    storeList = parsedList;
                    android.util.Log.d("MainActivity", "Parsed list size: " + parsedList.size());
                    showRestaurants();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStores(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ImageButton btnBasket = findViewById(R.id.btnBasket);
        btnBasket.setOnClickListener(v -> {
            Intent basketIntent = new Intent(this, BasketActivity.class);
            startActivity(basketIntent);
        });


        boolean filtersSet = checkIfFiltersExist();


    }

    private boolean checkIfFiltersExist() {
        Intent intent = getIntent();
        String cuisine = intent.getStringExtra("FILTER_CUISINE");
        String latitude = intent.getStringExtra("FILTER_LATITUDE");
        String longitude = intent.getStringExtra("FILTER_LONGITUDE");
        int stars = intent.getIntExtra("FILTER_STARS", 0);
        String price = intent.getStringExtra("FILTER_PRICE");
        boolean openNow = intent.getBooleanExtra("FILTER_OPEN_NOW", false);

        return ((cuisine != null && !cuisine.isEmpty())
                || (latitude != null && !latitude.isEmpty())
                || (longitude != null && !longitude.isEmpty())
                || stars > 0
                || (price != null && !price.isEmpty())
                || openNow);
    }

    public static List<Store> parseStores(String jsonString) throws JSONException {
        List<Store> stores = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Store store = Store.fromJson(obj); // assuming you have such a method
            stores.add(store);
        }
        return stores;
    }


    private void showRestaurants() {
        if (storeList == null || storeList.isEmpty()) {
            showNoFiltersMessage();
            return;
        }
        rvRestaurants.setVisibility(View.VISIBLE);
        tvNoFilters.setVisibility(View.GONE);
        rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        android.util.Log.d("MainActivity", "Parsed list inside showRestaurants : " + storeList);
        StoreAdapter adapter = new StoreAdapter(this, storeList);
        android.util.Log.d("MainActivity", "Adapter : " + adapter);
        rvRestaurants.setAdapter(adapter);
    }

    private void filterStores(String query) {
        List<Store> filteredList = new ArrayList<>();
        for (Store store : storeList) {
            if (store.getStoreName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(store);
            }
        }
        StoreAdapter adapter = new StoreAdapter(this, filteredList);
        rvRestaurants.setAdapter(adapter);
    }

    private void showNoFiltersMessage() {
        rvRestaurants.setVisibility(View.GONE);
        tvNoFilters.setVisibility(View.VISIBLE);
    }
}

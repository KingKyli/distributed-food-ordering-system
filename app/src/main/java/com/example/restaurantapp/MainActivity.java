package com.example.restaurantapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends BaseActivity {

    private RecyclerView rvRestaurants;
    private View emptyStateContainer;
    private View skeletonContainer;
    private TextView tvNoFilters;
    private TextView tvMainStatus;
    private ProgressBar progressRestaurants;
    private EditText etSearch;
    private ImageButton btnBasket;
    private Chip chipTopRated;
    private Chip chipPizza;
    private Chip chipBurgers;
    private Chip chipBudget;
    private Chip chipSaved;
    private final List<Store> baseStoreList = new ArrayList<>();
    private StoreAdapter storeAdapter;
    private volatile boolean activityActive;
    private final RestaurantRepository restaurantRepository = new RestaurantRepository();

    @Override
    protected String getBottomNavType() {
        return "home";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ActivityUtils.ensureConnectedOrRedirect(this)) {
            return;
        }

        setContentView(R.layout.activity_main);
        setupBottomNav();
        activityActive = true;

        rvRestaurants = findViewById(R.id.rvRestaurants);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        skeletonContainer = findViewById(R.id.skeletonContainer);
        tvNoFilters = findViewById(R.id.tvNoFilters);
        tvMainStatus = findViewById(R.id.tvMainStatus);
        progressRestaurants = findViewById(R.id.progressRestaurants);
        etSearch = findViewById(R.id.etSearch);
        btnBasket = findViewById(R.id.btnBasket);
        chipTopRated = findViewById(R.id.chipTopRated);
        chipPizza = findViewById(R.id.chipPizza);
        chipBurgers = findViewById(R.id.chipBurgers);
        chipBudget = findViewById(R.id.chipBudget);
        chipSaved = findViewById(R.id.chipSaved);

        // Pre-fill search if coming from Order History "Reorder"
        String incomingQuery = getIntent().getStringExtra("SEARCH_QUERY");
        if (incomingQuery != null && !incomingQuery.isEmpty()) {
            etSearch.setText(incomingQuery);
        }

        rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        storeAdapter = new StoreAdapter(this, new ArrayList<>());
        rvRestaurants.setAdapter(storeAdapter);

        SharedPreferences prefs = getSharedPreferences("filters_prefs", MODE_PRIVATE);
        Intent intent = getIntent();
        if (intent.getBooleanExtra("RESET_FILTERS", false)) {
            prefs.edit().clear().apply();
        }

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
                intent.getStringExtra("FILTER_PRICE") : prefs.getString("priceValue", "");

        View.OnClickListener quickFilterListener = v -> applyVisibleFilters();
        chipTopRated.setOnClickListener(quickFilterListener);
        chipPizza.setOnClickListener(quickFilterListener);
        chipBurgers.setOnClickListener(quickFilterListener);
        chipBudget.setOnClickListener(quickFilterListener);
        chipSaved.setOnClickListener(quickFilterListener);

        setLoadingState(true, getString(R.string.home_loading_restaurants));
        loadRestaurants(latitude, longitude, cuisine, stars, price, distance);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyVisibleFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnBasket.setOnClickListener(v -> {
            Intent basketIntent = new Intent(this, BasketActivity.class);
            startActivity(basketIntent);
        });
    }

    @Override
    protected void onDestroy() {
        activityActive = false;
        super.onDestroy();
    }

    private void loadRestaurants(String latitude, String longitude, String cuisine, int stars, String price, int distanceKm) {
        new Thread(() -> {
            AppResult<List<Store>> result = restaurantRepository.searchStores(latitude, longitude, cuisine, stars, price);
            ActivityUtils.runOnUiThreadIfAlive(this, () -> {
                if (!activityActive) {
                    return;
                }
                if (!result.isSuccess()) {
                    showErrorState(result.getMessage());
                    return;
                }

                List<Store> parsedList = applyClientFilters(result.getData(), latitude, longitude, cuisine, stars, price, distanceKm);
                baseStoreList.clear();
                baseStoreList.addAll(parsedList);
                applyVisibleFilters();
            });
        }).start();
    }

    public static List<Store> parseStores(String jsonString) throws JSONException {
        return StoreJsonParser.parseStores(jsonString);
    }

    private void applyVisibleFilters() {
        if (!activityActive) {
            return;
        }

        String query = etSearch.getText() == null ? "" : etSearch.getText().toString();
        List<Store> filteredList = new ArrayList<>();
        for (Store store : baseStoreList) {
            if (matchesQuickFilters(store) && matchesSearch(store, query)) {
                filteredList.add(store);
            }
        }

        if (baseStoreList.isEmpty()) {
            showEmptyState(getString(R.string.home_empty_selected_filters));
            return;
        }

        if (filteredList.isEmpty()) {
            // Specific message when "Saved" chip is active but no favorites saved
            if (chipSaved != null && chipSaved.isChecked()
                    && FavoritesRepository.getFavorites(this).isEmpty()) {
                showEmptyState("No saved restaurants yet.\nTap ♡ on any restaurant to save it.");
            } else {
                showEmptyState(getString(R.string.home_empty_filtered));
            }
            return;
        }

        showRestaurants(filteredList);
    }

    private void showRestaurants(List<Store> visibleStores) {
        setLoadingState(false, null);
        rvRestaurants.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);
        tvMainStatus.setVisibility(View.GONE);
        if (storeAdapter != null) {
            storeAdapter.updateStores(visibleStores);
        }
        etSearch.setEnabled(true);
        btnBasket.setEnabled(true);
    }

    private void setLoadingState(boolean loading, String message) {
        progressRestaurants.setVisibility(View.GONE); // always hidden — skeleton replaces it

        if (skeletonContainer != null) {
            skeletonContainer.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (loading) {
                // Pulse animation
                skeletonContainer.animate().cancel();
                skeletonContainer.setAlpha(1f);
                animateSkeleton();
            } else {
                skeletonContainer.animate().cancel();
                skeletonContainer.setAlpha(1f);
            }
        }

        tvMainStatus.setVisibility(message == null ? View.GONE : View.VISIBLE);
        if (message != null) tvMainStatus.setText(message);

        if (loading) {
            rvRestaurants.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.GONE);
        }
        etSearch.setEnabled(!loading);
        btnBasket.setEnabled(!loading);
    }

    private void animateSkeleton() {
        if (skeletonContainer == null || skeletonContainer.getVisibility() != View.VISIBLE) return;
        skeletonContainer.animate()
                .alpha(0.4f)
                .setDuration(700)
                .withEndAction(() -> {
                    if (skeletonContainer.getVisibility() == View.VISIBLE) {
                        skeletonContainer.animate()
                                .alpha(1f)
                                .setDuration(700)
                                .withEndAction(this::animateSkeleton)
                                .start();
                    }
                })
                .start();
    }

    private void showEmptyState(String message) {
        setLoadingState(false, null);
        rvRestaurants.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
        tvMainStatus.setVisibility(View.GONE);
        tvNoFilters.setText(message);
        etSearch.setEnabled(true);
        btnBasket.setEnabled(true);
    }

    private void showErrorState(String message) {
        setLoadingState(false, null);
        rvRestaurants.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
        tvNoFilters.setText(message);
        tvMainStatus.setText(message);
        tvMainStatus.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        etSearch.setEnabled(false);
        btnBasket.setEnabled(true);
    }

    private List<Store> applyClientFilters(List<Store> stores, String latitude, String longitude, String cuisine, int minStars, String price, int distanceKm) {
        List<Store> filtered = new ArrayList<>();
        Double userLat = parseDouble(latitude);
        Double userLon = parseDouble(longitude);

        for (Store store : stores) {
            // Price filter - normalize comparison (handle $, $$, $$$)
            if (price != null && !price.isEmpty()) {
                String storePrice = store.getPriceCategory();
                if (storePrice == null || !normalizePrice(storePrice).equals(normalizePrice(price))) {
                    continue;
                }
            }

            // Stars filter - store must have at least minStars
            if (minStars > 0) {
                Integer storeStarsInt = store.getStoreStars();
                double storeStars = storeStarsInt != null ? storeStarsInt : 0;
                if (storeStars < minStars) {
                    continue;
                }
            }
            
            // Cuisine/Food Category filter
            if (cuisine != null && !cuisine.isEmpty() && !cuisine.equalsIgnoreCase("All")) {
                String storeCategory = store.getFoodCategory();
                if (storeCategory == null
                        || !storeCategory.toLowerCase(Locale.ROOT).contains(cuisine.toLowerCase(Locale.ROOT))) {
                    continue;
                }
            }

            // Distance filter
            if (distanceKm > 0 && userLat != null && userLon != null && store.getStoreLat() != null && store.getStoreLon() != null) {
                double distance = distanceKm(userLat, userLon, store.getStoreLat(), store.getStoreLon());
                if (distance > distanceKm) {
                    continue;
                }
            }

            filtered.add(store);
        }

        return filtered;
    }

    private boolean matchesQuickFilters(Store store) {
        // Saved / Favorites filter — highest priority
        if (chipSaved != null && chipSaved.isChecked()) {
            if (!FavoritesRepository.isFavorite(this, store.getStoreName())) {
                return false;
            }
        }

        if (chipTopRated.isChecked() && !hasTopRating(store)) {
            return false;
        }

        if (chipBudget.isChecked() && !"$".equals(normalizePrice(store.getPriceCategory()))) {
            return false;
        }

        List<String> selectedCuisineKeywords = new ArrayList<>();
        if (chipPizza.isChecked()) {
            selectedCuisineKeywords.add("pizza");
        }
        if (chipBurgers.isChecked()) {
            selectedCuisineKeywords.add("burger");
        }

        if (selectedCuisineKeywords.isEmpty()) {
            return true;
        }

        for (String keyword : selectedCuisineKeywords) {
            if (matchesCategoryKeyword(store, keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private String normalizePrice(String price) {
        if (price == null) return "";
        // Remove spaces and convert to standard format
        String normalized = price.trim().replaceAll("\\s+", "");
        // Count $ signs
        int dollarCount = 0;
        for (char c : normalized.toCharArray()) {
            if (c == '$' || c == '€') dollarCount++;
        }
        if (dollarCount == 0) {
            // Try to interpret words
            String lower = normalized.toLowerCase(Locale.ROOT);
            if (lower.contains("cheap") || lower.contains("budget") || lower.equals("$")) return "$";
            if (lower.contains("expensive") || lower.contains("luxury") || lower.equals("$$$")) return "$$$";
            return "$$"; // default medium
        }
        if (dollarCount == 1) return "$";
        if (dollarCount == 2) return "$$";
        return "$$$";
    }

    private boolean matchesSearch(Store store, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);

        if ((normalizedQuery.contains("budget") || normalizedQuery.contains("cheap"))
                && "$".equals(normalizePrice(store.getPriceCategory()))) {
            return true;
        }

        if ((normalizedQuery.contains("top") || normalizedQuery.contains("rated")) && hasTopRating(store)) {
            return true;
        }

        String normalizedQueryPrice = normalizePrice(normalizedQuery);
        if (isPriceSearchTerm(normalizedQuery)
                && !TextUtils.isEmpty(normalizedQueryPrice)
                && normalizedQueryPrice.equals(normalizePrice(store.getPriceCategory()))) {
            return true;
        }

        if (store.getStoreName() != null
            && store.getStoreName().toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
            return true;
        }

        if (store.getFoodCategory() != null
            && store.getFoodCategory().toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
            return true;
        }

        if (store.getProducts() != null) {
            for (Product product : store.getProducts()) {
                if (product.getProductName() != null
                        && product.getProductName().toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                    return true;
                }
                if (product.getProductType() != null
                        && product.getProductType().toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasTopRating(Store store) {
        Integer stars = store.getStoreStars();
        return stars != null && stars >= 4;
    }

    private boolean matchesCategoryKeyword(Store store, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);

        if (store.getFoodCategory() != null
            && store.getFoodCategory().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
            return true;
        }

        if (store.getStoreName() != null
            && store.getStoreName().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
            return true;
        }

        if (store.getProducts() == null) {
            return false;
        }

        for (Product product : store.getProducts()) {
            if (product.getProductName() != null
                    && product.getProductName().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                return true;
            }
            if (product.getProductType() != null
                    && product.getProductType().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPriceSearchTerm(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        return query.contains("$")
                || query.contains("€")
                || query.contains("budget")
                || query.contains("cheap")
                || query.contains("mid")
                || query.contains("medium")
                || query.contains("expensive")
                || query.contains("luxury");
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}

package com.example.restaurantapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FiltersActivity extends BaseActivity {

    private Spinner spinnerCuisine;
    private SeekBar seekBarDistance;
    private TextView distanceValue;
    private EditText editLatitude, editLongitude;
    private Spinner spinnerStars;
    private RadioGroup radioPrice;
    private CheckBox checkboxOpenNow;
    private static final String PREFS_FILTERS = "filters_prefs";



    @Override
    protected String getBottomNavType() {
        return "filters";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        setupBottomNav();
        SharedPreferences prefs = getSharedPreferences(PREFS_FILTERS, MODE_PRIVATE);

        spinnerCuisine = findViewById(R.id.spinner_cuisine);
        seekBarDistance = findViewById(R.id.seekbar_distance);
        distanceValue = findViewById(R.id.text_distance_value);
        editLatitude = findViewById(R.id.edit_latitude);
        editLongitude = findViewById(R.id.edit_longitude);
        spinnerStars = findViewById(R.id.spinner_stars);
        radioPrice = findViewById(R.id.radio_price);
        checkboxOpenNow = findViewById(R.id.checkbox_open_now);
        checkboxOpenNow.setChecked(false);
        checkboxOpenNow.setVisibility(View.GONE);

        List<String> starOptions = new ArrayList<>();
        starOptions.add("Any");
        starOptions.addAll(Arrays.asList("1", "2", "3", "4", "5"));
        ArrayAdapter<String> starAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, starOptions);
        starAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStars.setAdapter(starAdapter);


        // Example cuisine options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.cuisine_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCuisine.setAdapter(adapter);

        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceValue.setText(formatDistance(progress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Restore filters from SharedPreferences
        String savedCuisine = prefs.getString("cuisine", "Any");
        int cuisinePos = adapter.getPosition(savedCuisine.isEmpty() ? "Any" : savedCuisine);
        if (cuisinePos >= 0) spinnerCuisine.setSelection(cuisinePos);
        seekBarDistance.setProgress(prefs.getInt("distance", 0));
        distanceValue.setText(formatDistance(prefs.getInt("distance", 0)));
        editLatitude.setText(prefs.getString("latitude", ""));
        editLongitude.setText(prefs.getString("longitude", ""));
        spinnerStars.setSelection(prefs.getInt("starsPosition", 0));
        int priceIndex = prefs.getInt("price", -1);
        if (priceIndex != -1 && priceIndex < radioPrice.getChildCount()) {
            radioPrice.check(radioPrice.getChildAt(priceIndex).getId());
        }
        checkboxOpenNow.setChecked(false);

        // Collect filter data and start MainActivity with extras
        Button applyButton = findViewById(R.id.button_apply_filters);
        applyButton.setOnClickListener(v -> {
            String cuisine = spinnerCuisine.getSelectedItem() != null ? spinnerCuisine.getSelectedItem().toString() : "";
            if ("Any".equalsIgnoreCase(cuisine)) {
                cuisine = "";
            }
            int distance = seekBarDistance.getProgress();
            String latitude = editLatitude.getText() != null ? editLatitude.getText().toString().trim() : "";
            String longitude = editLongitude.getText() != null ? editLongitude.getText().toString().trim() : "";

            int stars = 0;
            Object selectedStar = spinnerStars.getSelectedItem();
            if (selectedStar != null && !"Any".equalsIgnoreCase(selectedStar.toString())) {
                try {
                    stars = Integer.parseInt(selectedStar.toString());
                } catch (NumberFormatException e) {
                    stars = 0;
                }
            }

            int priceId = radioPrice.getCheckedRadioButtonId();
            String price = "";
            if (priceId != -1) {
                RadioButton selectedPrice = findViewById(priceId);
                price = selectedPrice != null ? selectedPrice.getText().toString() : "";
            }
            Intent intent = new Intent(FiltersActivity.this, MainActivity.class);
            intent.putExtra("FILTER_CUISINE", cuisine);
            intent.putExtra("FILTER_DISTANCE", distance);
            intent.putExtra("FILTER_LATITUDE", latitude);
            intent.putExtra("FILTER_LONGITUDE", longitude);
            intent.putExtra("FILTER_STARS", stars);
            intent.putExtra("FILTER_PRICE", price);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("cuisine", cuisine);
            editor.putInt("distance", distance);
            editor.putString("latitude", latitude);
            editor.putString("longitude", longitude);
            editor.putInt("stars", stars);
            editor.putInt("starsPosition", spinnerStars.getSelectedItemPosition());
            editor.putInt("price", radioPrice.indexOfChild(findViewById(priceId)));
            editor.putString("priceValue", price);
            editor.putBoolean("openNow", false);
            editor.apply();

            startActivity(intent);
            finish();
        });
    }

    private String formatDistance(int distance) {
        return distance + " km";
    }
}

package com.example.restaurantapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class FiltersActivity extends BaseActivity {

    private Spinner spinnerCuisine;
    private SeekBar seekBarDistance;
    private TextView distanceValue;
    private EditText editLatitude, editLongitude;
    private Spinner spinnerStars;
    private RadioGroup radioPrice;
    private CheckBox checkboxOpenNow;
    private static final String PREFS_FILTERS = "filters_prefs";

    private static final String KEY_CUISINE = "cuisine";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_STARS = "stars";
    private static final String KEY_PRICE_ID = "priceId";
    private static final String KEY_PRICE_TEXT = "priceText";
    private static final String KEY_OPEN_NOW = "openNow";



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

        // Stars: include "Any" (0) to avoid forcing a filter
        List<String> starsOptions = Arrays.asList("Any", "1", "2", "3", "4", "5");
        ArrayAdapter<String> starAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, starsOptions);
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
                distanceValue.setText(progress + " km");
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Restore filters from SharedPreferences
        String savedCuisine = prefs.getString(KEY_CUISINE, "Any");
        if (savedCuisine != null) {
            int cuisinePos = adapter.getPosition(savedCuisine.isEmpty() ? "Any" : savedCuisine);
            if (cuisinePos >= 0) spinnerCuisine.setSelection(cuisinePos);
        }
        int savedDistance = prefs.getInt(KEY_DISTANCE, 0);
        seekBarDistance.setProgress(savedDistance);
        distanceValue.setText(savedDistance + " km");
        editLatitude.setText(prefs.getString(KEY_LATITUDE, ""));
        editLongitude.setText(prefs.getString(KEY_LONGITUDE, ""));

        int savedStars = prefs.getInt(KEY_STARS, 0);
        if (savedStars >= 0 && savedStars <= 5) {
            spinnerStars.setSelection(savedStars);
        }

        int savedPriceId = prefs.getInt(KEY_PRICE_ID, -1);
        if (savedPriceId != -1) {
            radioPrice.check(savedPriceId);
        }
        checkboxOpenNow.setChecked(prefs.getBoolean(KEY_OPEN_NOW, false));

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
            if (selectedStar != null) {
                String s = selectedStar.toString();
                if (!"Any".equalsIgnoreCase(s)) {
                    try {
                        stars = Integer.parseInt(s);
                    } catch (NumberFormatException ignored) {
                        stars = 0;
                    }
                }
            }

            int priceId = radioPrice.getCheckedRadioButtonId();
            String price = "";
            if (priceId != -1) {
                RadioButton selectedPrice = findViewById(priceId);
                price = selectedPrice != null ? selectedPrice.getText().toString() : "";
            }
            boolean openNow = checkboxOpenNow != null && checkboxOpenNow.isChecked();

            Intent intent = new Intent(FiltersActivity.this, MainActivity.class);
            intent.putExtra("FILTER_CUISINE", cuisine);
            intent.putExtra("FILTER_DISTANCE", distance);
            intent.putExtra("FILTER_LATITUDE", latitude);
            intent.putExtra("FILTER_LONGITUDE", longitude);
            intent.putExtra("FILTER_STARS", stars);
            intent.putExtra("FILTER_PRICE", price);
            intent.putExtra("FILTER_OPEN_NOW", openNow);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_CUISINE, cuisine);
            editor.putInt(KEY_DISTANCE, distance);
            editor.putString(KEY_LATITUDE, latitude);
            editor.putString(KEY_LONGITUDE, longitude);
            editor.putInt(KEY_STARS, stars);
            editor.putInt(KEY_PRICE_ID, priceId);
            editor.putString(KEY_PRICE_TEXT, price);
            editor.putBoolean(KEY_OPEN_NOW, openNow);
            editor.apply();

            startActivity(intent);
            finish();
        });
    }
}

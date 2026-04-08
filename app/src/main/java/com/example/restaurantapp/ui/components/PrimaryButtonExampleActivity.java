package com.example.restaurantapp.ui.components;

import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.restaurantapp.R;

/**
 * Example Activity demonstrating PrimaryButton usage
 * 
 * This shows all button states and use cases
 */
public class PrimaryButtonExampleActivity extends AppCompatActivity {
    
    private PrimaryButton checkoutButton;
    private PrimaryButton placeOrderButton;
    private PrimaryButton addToCartButton;
    private PrimaryButton disabledButton;
    private PrimaryButton noIconButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create layout programmatically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);
        setContentView(layout);
        
        // Example 1: Checkout Button (Default)
        checkoutButton = new PrimaryButton(this);
        checkoutButton.setText("CHECKOUT");
        checkoutButton.setOnClickListener(v -> {
            Toast.makeText(this, "Checkout clicked!", Toast.LENGTH_SHORT).show();
        });
        addButtonWithMargin(layout, checkoutButton, "DEFAULT STATE");
        
        // Example 2: Place Order Button (with loading)
        placeOrderButton = new PrimaryButton(this);
        placeOrderButton.setText("PLACE ORDER");
        placeOrderButton.setOnClickListener(v -> {
            placeOrderButton.setLoading(true);
            
            // Simulate API call
            new Handler().postDelayed(() -> {
                placeOrderButton.setLoading(false);
                Toast.makeText(this, "Order placed successfully!", 
                    Toast.LENGTH_SHORT).show();
            }, 2000);
        });
        addButtonWithMargin(layout, placeOrderButton, "LOADING STATE (click to test)");
        
        // Example 3: Add to Cart Button
        addToCartButton = new PrimaryButton(this);
        addToCartButton.setText("ADD TO CART");
        addToCartButton.setOnClickListener(v -> {
            Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show();
        });
        addButtonWithMargin(layout, addToCartButton, "WITH ICON");
        
        // Example 4: Disabled Button
        disabledButton = new PrimaryButton(this);
        disabledButton.setText("CHECKOUT");
        disabledButton.setEnabled(false);
        addButtonWithMargin(layout, disabledButton, "DISABLED STATE");
        
        // Example 5: Button without icon
        noIconButton = new PrimaryButton(this);
        noIconButton.setText("REORDER");
        noIconButton.setShowIcon(false);
        noIconButton.setOnClickListener(v -> {
            Toast.makeText(this, "Reorder clicked!", Toast.LENGTH_SHORT).show();
        });
        addButtonWithMargin(layout, noIconButton, "WITHOUT ICON");
    }
    
    private void addButtonWithMargin(LinearLayout layout, PrimaryButton button, String label) {
        // Add label
        android.widget.TextView labelView = new android.widget.TextView(this);
        labelView.setText(label);
        labelView.setTextSize(12);
        labelView.setTextColor(0xFF64748B);
        labelView.setPadding(0, 32, 0, 8);
        layout.addView(labelView);
        
        // Add button
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        button.setLayoutParams(params);
        layout.addView(button);
    }
}


package com.example.restaurantapp.ui.components;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Example Activity demonstrating SecondaryButton usage
 */
public class SecondaryButtonExampleActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);
        layout.setBackgroundColor(0xFFF8FAFC);
        setContentView(layout);
        
        // Example 1: Basic Secondary Button
        SecondaryButton btn1 = new SecondaryButton(this);
        btn1.setText("View Menu");
        btn1.setOnClickListener(v -> 
            Toast.makeText(this, "View Menu clicked!", Toast.LENGTH_SHORT).show()
        );
        addButtonWithLabel(layout, btn1, "BASIC - No Icon");
        
        // Example 2: With Icon
        SecondaryButton btn2 = new SecondaryButton(this);
        btn2.setText("View Menu");
        btn2.setShowIcon(true);
        btn2.setIconColor(0xFFF97316); // Orange
        btn2.setOnClickListener(v -> 
            Toast.makeText(this, "With icon clicked!", Toast.LENGTH_SHORT).show()
        );
        addButtonWithLabel(layout, btn2, "WITH ICON");
        
        // Example 3: See More
        SecondaryButton btn3 = new SecondaryButton(this);
        btn3.setText("See More");
        btn3.setOnClickListener(v -> 
            Toast.makeText(this, "See More clicked!", Toast.LENGTH_SHORT).show()
        );
        addButtonWithLabel(layout, btn3, "SEE MORE");
        
        // Example 4: Open
        SecondaryButton btn4 = new SecondaryButton(this);
        btn4.setText("Open");
        btn4.setOnClickListener(v -> 
            Toast.makeText(this, "Open clicked!", Toast.LENGTH_SHORT).show()
        );
        addButtonWithLabel(layout, btn4, "OPEN");
        
        // Example 5: Details
        SecondaryButton btn5 = new SecondaryButton(this);
        btn5.setText("Details");
        btn5.setOnClickListener(v -> 
            Toast.makeText(this, "Details clicked!", Toast.LENGTH_SHORT).show()
        );
        addButtonWithLabel(layout, btn5, "DETAILS");
        
        // Example 6: Disabled
        SecondaryButton btn6 = new SecondaryButton(this);
        btn6.setText("Disabled");
        btn6.setEnabled(false);
        addButtonWithLabel(layout, btn6, "DISABLED STATE");
    }
    
    private void addButtonWithLabel(LinearLayout layout, SecondaryButton button, String label) {
        // Add label
        android.widget.TextView labelView = new android.widget.TextView(this);
        labelView.setText(label);
        labelView.setTextSize(12);
        labelView.setTextColor(0xFF64748B);
        labelView.setPadding(0, 32, 0, 8);
        layout.addView(labelView);
        
        // Add button
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        button.setLayoutParams(params);
        layout.addView(button);
    }
}


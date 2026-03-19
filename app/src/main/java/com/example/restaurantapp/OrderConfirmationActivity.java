package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrderConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        TextView tvOrderId = findViewById(R.id.tvOrderId);
        TextView tvConfStoreName = findViewById(R.id.tvConfStoreName);
        TextView tvConfItemCount = findViewById(R.id.tvConfItemCount);
        TextView tvConfTotal = findViewById(R.id.tvConfTotal);
        TextView tvConfAddress = findViewById(R.id.tvConfAddress);
        Button btnBackToHome = findViewById(R.id.btnBackToHome);

        // Generate a simple order ID
        String orderId = "ORD-" + System.currentTimeMillis() % 100000;
        tvOrderId.setText("Order #" + orderId);

        // Populate from intent extras
        String storeName = getIntent().getStringExtra("store_name");
        double totalPrice = getIntent().getDoubleExtra("total_price", 0);
        int itemCount = getIntent().getIntExtra("item_count", 0);
        String deliveryAddress = getIntent().getStringExtra("delivery_address");

        tvConfStoreName.setText(storeName != null ? storeName : "—");
        tvConfItemCount.setText(itemCount + (itemCount == 1 ? " item" : " items"));
        tvConfTotal.setText(String.format("€%.2f", totalPrice));
        tvConfAddress.setText(deliveryAddress != null ? deliveryAddress : "—");

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(OrderConfirmationActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Navigate to home instead of back to basket
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

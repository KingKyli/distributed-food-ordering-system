// src/main/java/com/example/restaurantapp/BasketActivity.java
package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Map;

public class BasketActivity extends AppCompatActivity {
    private BasketAdapter basketAdapter;
    private TextView tvItemCount;
    private TextView tvTotalPrice;
    private EditText etDeliveryAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        tvItemCount = findViewById(R.id.tvItemCount);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        RecyclerView rvBasket = findViewById(R.id.rvBasket);
        rvBasket.setLayoutManager(new LinearLayoutManager(this));
        basketAdapter = new BasketAdapter(this);
        rvBasket.setAdapter(basketAdapter);
        updateItemCount();
        updateTotalPrice();

        Button btnBuy = findViewById(R.id.btnBuy);
        btnBuy.setOnClickListener(v -> performBuy());
    }

    private void performBuy() {
        if (Basket.getInstance().getItems().isEmpty()) {
            Toast.makeText(this, "Your basket is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String deliveryAddress = etDeliveryAddress.getText().toString().trim();
        if (deliveryAddress.isEmpty()) {
            etDeliveryAddress.setError("Please enter a delivery address");
            etDeliveryAddress.requestFocus();
            return;
        }

        new Thread(() -> {
            MasterCommunicator comm = ServerConnection.getInstance();
            final boolean[] allOk = {true};
            String storeName = Basket.getInstance().getStoreName();
            double totalPrice = 0;
            int totalItems = 0;

            for (Map.Entry<Product, Integer> entry : Basket.getInstance().getItems().entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();
                totalPrice += product.getPrice() * quantity;
                totalItems += quantity;
                boolean ok = comm.sendBuyRequest(storeName, product.getProductName(), quantity);
                if (!ok) allOk[0] = false;
            }

            final double finalTotal = totalPrice;
            final int finalItemCount = totalItems;
            final String finalStoreName = storeName;

            runOnUiThread(() -> {
                if (allOk[0]) {
                    Basket.getInstance().clear();
                    notifyBasketChanged();

                    Intent intent = new Intent(BasketActivity.this, OrderConfirmationActivity.class);
                    intent.putExtra("store_name", finalStoreName);
                    intent.putExtra("total_price", finalTotal);
                    intent.putExtra("item_count", finalItemCount);
                    intent.putExtra("delivery_address", deliveryAddress);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Some items could not be purchased.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void updateItemCount() {
        int count = 0;
        for (Integer qty : Basket.getInstance().getItems().values()) {
            count += qty;
        }
        tvItemCount.setText(count + (count == 1 ? " item" : " items"));
    }

    private void updateTotalPrice() {
        double total = 0;
        for (Map.Entry<Product, Integer> entry : Basket.getInstance().getItems().entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        tvTotalPrice.setText(String.format("Total: €%.2f", total));
    }

    @Override
    protected void onResume() {
        super.onResume();
        basketAdapter.updateItems();
        updateItemCount();
        updateTotalPrice();
    }

    // Call this from BasketAdapter after any change
    public void notifyBasketChanged() {
        basketAdapter.updateItems();
        updateItemCount();
        updateTotalPrice();
    }
}

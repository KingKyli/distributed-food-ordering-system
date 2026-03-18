// src/main/java/com/example/restaurantapp/BasketActivity.java
package com.example.restaurantapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Map;

public class BasketActivity extends AppCompatActivity {
    private BasketAdapter basketAdapter;
    private TextView tvItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        tvItemCount = findViewById(R.id.tvItemCount);
        RecyclerView rvBasket = findViewById(R.id.rvBasket);
        rvBasket.setLayoutManager(new LinearLayoutManager(this));
        basketAdapter = new BasketAdapter(this);
        rvBasket.setAdapter(basketAdapter);
        updateItemCount();

        Button btnBuy = findViewById(R.id.btnBuy);
        btnBuy.setOnClickListener(v -> performBuy());
    }

    private void performBuy() {
        new Thread(() -> {
            MasterCommunicator comm = ConnectionUtils.requireConnected(this);
            if (comm == null) return;
            final boolean[] allOk = {true};
            String storeName = Basket.getInstance().getStoreName();
            for (Map.Entry<Product, Integer> entry : Basket.getInstance().getItems().entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();
                boolean ok = comm.sendBuyRequest(storeName, product.getProductName(), quantity);
                if (!ok) allOk[0] = false;
            }
            runOnUiThread(() -> {
                if (allOk[0]) {
                    Basket.getInstance().clear();
                    notifyBasketChanged();
                    Toast.makeText(this, "Purchase successful!", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        basketAdapter.updateItems();
        updateItemCount();
    }

    // Call this from BasketAdapter after any change
    public void notifyBasketChanged() {
        basketAdapter.updateItems();
        updateItemCount();
    }
}


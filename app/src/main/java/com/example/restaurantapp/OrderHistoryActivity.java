package com.example.restaurantapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private volatile boolean activityActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        activityActive = true;

        ImageButton btnBack = findViewById(R.id.btnHistoryBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvOrderHistory);
        View emptyState = findViewById(R.id.emptyHistoryState);
        rv.setLayoutManager(new LinearLayoutManager(this));

        new Thread(() -> {
            List<OrderRecord> orders = OrderHistoryRepository.getOrders(this);
            ActivityUtils.runOnUiThreadIfAlive(this, () -> {
                if (!activityActive) {
                    return;
                }

                if (orders.isEmpty()) {
                    rv.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    rv.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);
                    rv.setAdapter(new OrderHistoryAdapter(orders));
                }
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        activityActive = false;
        super.onDestroy();
    }
}


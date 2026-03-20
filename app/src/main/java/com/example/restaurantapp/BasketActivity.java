package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.button.MaterialButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class BasketActivity extends AppCompatActivity {
    private BasketAdapter basketAdapter;
    private TextView tvItemCount;
    private TextView tvBasketStore;
    private TextView tvBasketTotal;
    private TextView tvBasketStatus;
    private MaterialButton btnBuy;
    private ProgressBar progressBasket;
    private volatile boolean activityActive;
    private OrderService orderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ActivityUtils.ensureConnectedOrRedirect(this)) {
            return;
        }

        setContentView(R.layout.activity_basket);
        activityActive = true;
        orderService = new OrderService(this);

        tvItemCount = findViewById(R.id.tvItemCount);
        tvBasketStore = findViewById(R.id.tvBasketStore);
        tvBasketTotal = findViewById(R.id.tvBasketTotal);
        tvBasketStatus = findViewById(R.id.tvBasketStatus);
        progressBasket = findViewById(R.id.progressBasket);
        RecyclerView rvBasket = findViewById(R.id.rvBasket);
        rvBasket.setLayoutManager(new LinearLayoutManager(this));
        basketAdapter = new BasketAdapter(this);
        rvBasket.setAdapter(basketAdapter);
        updateSummary();

        btnBuy = findViewById(R.id.btnBuy);
        btnBuy.setOnClickListener(v -> performBuy());

        // Order history link
        TextView tvHistoryLink = findViewById(R.id.tvHistoryLink);
        if (tvHistoryLink != null) {
            tvHistoryLink.setPaintFlags(tvHistoryLink.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            tvHistoryLink.setOnClickListener(v ->
                    startActivity(new Intent(this, OrderHistoryActivity.class)));
        }

        refreshBasketState();
    }

    @Override
    protected void onDestroy() {
        activityActive = false;
        super.onDestroy();
    }

    private void performBuy() {
        List<BasketItem> basketSnapshot = Basket.getInstance().getItems();
        if (basketSnapshot.isEmpty()) {
            setStatus("Your basket is empty.", false);
            return;
        }

        setPurchaseLoading(true);
        new Thread(() -> {
            AppResult<Void> result = orderService.submitOrder(basketSnapshot);
            ActivityUtils.runOnUiThreadIfAlive(this, () -> {
                if (!activityActive) {
                    return;
                }
                setPurchaseLoading(false);
                if (result.isSuccess()) {
                    Basket.getInstance().clear();
                    notifyBasketChanged();
                    setStatus("Purchase successful!", false);
                    Toast.makeText(this, "Purchase successful!", Toast.LENGTH_SHORT).show();
                } else {
                    setStatus(result.getMessage(), true);
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void updateSummary() {
        int count = Basket.getInstance().getItemCount();
        tvItemCount.setText(count + (count == 1 ? " item" : " items"));
        String storeName = Basket.getInstance().getStoreName();
        tvBasketStore.setText(storeName == null || storeName.trim().isEmpty()
                ? "No store selected"
                : "Store: " + storeName);
        tvBasketTotal.setText(String.format(Locale.getDefault(), "Total: \u20AC%.2f", Basket.getInstance().getTotalPrice()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        basketAdapter.updateItems();
        updateSummary();
        refreshBasketState();
    }

    // Call this from BasketAdapter after any change
    public void notifyBasketChanged() {
        basketAdapter.updateItems();
        updateSummary();
        refreshBasketState();
    }

    private void refreshBasketState() {
        boolean isEmpty = Basket.getInstance().isEmpty();
        btnBuy.setEnabled(!isEmpty && ServerConnection.isReady());
        if (isEmpty) {
            setStatus("Your basket is empty.", false);
        } else if (!ServerConnection.isReady()) {
            setStatus("Reconnect to the server before completing your purchase.", true);
        } else {
            tvBasketStatus.setVisibility(android.view.View.GONE);
        }
    }

    private void setPurchaseLoading(boolean loading) {
        progressBasket.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        btnBuy.setEnabled(!loading && !Basket.getInstance().isEmpty() && ServerConnection.isReady());
    }

    private void setStatus(String message, boolean isError) {
        tvBasketStatus.setText(message);
        tvBasketStatus.setTextColor(isError ? 0xFFFF0000 : 0xFF666666);
        tvBasketStatus.setVisibility(android.view.View.VISIBLE);
    }
}

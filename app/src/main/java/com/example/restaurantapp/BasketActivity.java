// src/main/java/com/example/restaurantapp/BasketActivity.java
package com.example.restaurantapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class BasketActivity extends AppCompatActivity {
    private BasketAdapter basketAdapter;
    private TextView tvItemCount;
    private TextView tvBasketStore;
    private TextView tvSubtotal;
    private TextView tvBasketTotal;
    private TextView tvBasketStatus;
    private Button btnBuy;
    private ProgressBar progressBasket;
    private volatile boolean activityActive;
    private final OrderService orderService = new OrderService();

    private static final double DELIVERY_FEE = 1.99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ActivityUtils.ensureConnectedOrRedirect(this)) {
            return;
        }

        setContentView(R.layout.activity_basket);
        activityActive = true;

        tvItemCount = findViewById(R.id.tvItemCount);
        tvBasketStore = findViewById(R.id.tvBasketStore);
        tvSubtotal = findViewById(R.id.tvSubtotal);
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

        ImageButton btnClearBasket = findViewById(R.id.btnClearBasket);
        btnClearBasket.setOnClickListener(v -> confirmClearBasket());

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
        double subtotal = Basket.getInstance().getTotalPrice();
        boolean hasItems = !Basket.getInstance().isEmpty();
        tvSubtotal.setText(String.format(Locale.getDefault(), "\u20ac%.2f", subtotal));
        double total = hasItems ? subtotal + DELIVERY_FEE : 0.0;
        tvBasketTotal.setText(String.format(Locale.getDefault(), "\u20ac%.2f", total));
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

    private void confirmClearBasket() {
        if (Basket.getInstance().isEmpty()) return;
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.basket_clear_title))
                .setMessage(getString(R.string.basket_clear_message))
                .setPositiveButton(getString(R.string.basket_clear_confirm), (dialog, which) -> {
                    Basket.getInstance().clear();
                    notifyBasketChanged();
                })
                .setNegativeButton(getString(R.string.common_cancel), null)
                .show();
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


package com.example.restaurantapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.BasketViewHolder> {
    private List<Map.Entry<Product, Integer>> items = new ArrayList<>();
    private final BasketActivity basketActivity;

    public BasketAdapter(BasketActivity activity) {
        this.basketActivity = activity;
        items.addAll(Basket.getInstance().getItems().entrySet());
    }

    @NonNull
    @Override
    public BasketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_basket_product, parent, false);
        return new BasketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BasketViewHolder holder, int position) {
        Map.Entry<Product, Integer> entry = items.get(position);
        Product product = entry.getKey();
        int quantity = entry.getValue();
        holder.tvName.setText(product.getProductName());
        holder.tvQuantity.setText("x" + quantity);
        holder.tvPrice.setText(String.format("€%.2f", product.getPrice() * quantity));
        holder.tvQuantityField.setText(String.valueOf(quantity));
        holder.ivPlus.setOnClickListener(v -> {
            Basket.getInstance().addProduct(product);
            basketActivity.notifyBasketChanged();
        });
        holder.ivMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                Basket.getInstance().removeProduct(product);
                basketActivity.notifyBasketChanged();
            } else if (quantity == 1) {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Remove Item")
                        .setMessage("Are you sure you want to remove this item from the basket?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Basket.getInstance().removeProduct(product);
                            basketActivity.notifyBasketChanged();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems() {
        items.clear();
        items.addAll(Basket.getInstance().getItems().entrySet());
        notifyDataSetChanged();
    }

    static class BasketViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvPrice, tvQuantityField;
        ImageView ivPlus, ivMinus;
        public BasketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBasketProductName);
            tvQuantity = itemView.findViewById(R.id.tvBasketProductQuantity);
            tvPrice = itemView.findViewById(R.id.tvBasketProductPrice);
            tvQuantityField = itemView.findViewById(R.id.tvBasketQuantityField);
            ivPlus = itemView.findViewById(R.id.ivCartPlus);
            ivMinus = itemView.findViewById(R.id.ivCartMinus);
        }
    }
}

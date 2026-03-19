package com.example.restaurantapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private final Context context;
    private final List<Store> storeList;

    public StoreAdapter(Context context, List<Store> storeList) {
        this.context = context;
        this.storeList = storeList;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = storeList.get(position);

        holder.tvName.setText(store.getStoreName());
        holder.tvPrice.setText(store.getPriceCategory());
        holder.tvCategory.setText(store.getFoodCategory());
        
        // Set stars with proper display
        Integer starsInt = store.getStoreStars();
        double stars = starsInt != null ? starsInt : 0;
        holder.tvStars.setText(getStarsString(stars));
        
        // Set numeric rating if tvRating exists
        if (holder.tvRating != null) {
            holder.tvRating.setText(String.format("%.1f", stars));
        }
        
        // Set price color based on category
        String priceCategory = store.getPriceCategory();
        if (priceCategory != null) {
            int priceColor;
            if (priceCategory.equals("$")) {
                priceColor = ContextCompat.getColor(context, R.color.price_cheap);
            } else if (priceCategory.equals("$$$")) {
                priceColor = ContextCompat.getColor(context, R.color.price_expensive);
            } else {
                priceColor = ContextCompat.getColor(context, R.color.price_medium);
            }
            holder.tvPrice.setTextColor(priceColor);
        }

        holder.btnView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RestaurantDetailsActivity.class);
            intent.putExtra("store_json", store.toJson().toString());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvCategory, tvStars, tvRating;
        Button btnView;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRestaurantName);
            tvPrice = itemView.findViewById(R.id.tvPriceCategory);
            tvCategory = itemView.findViewById(R.id.tvFoodCategory);
            tvStars = itemView.findViewById(R.id.tvStars);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }

    private String getStarsString(double stars) {
        int fullStars = (int) stars;
        boolean hasHalfStar = (stars - fullStars) >= 0.5;
        int emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fullStars; i++) sb.append("★");
        if (hasHalfStar) sb.append("★"); // Use full star for half (simpler)
        for (int i = 0; i < emptyStars; i++) sb.append("☆");
        return sb.toString();
    }
}
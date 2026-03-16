package com.example.restaurantapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
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
        android.util.Log.d("StoreAdapter", "I got this : " + store);

        holder.tvName.setText(store.getStoreName());
        holder.tvPrice.setText(store.getPriceCategory());
        holder.tvCategory.setText(store.getFoodCategory());
        holder.tvStars.setText(getStarsString(store.getStoreStars()));

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
        TextView tvName, tvPrice, tvCategory, tvStars;
        Button btnView;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRestaurantName);
            tvPrice = itemView.findViewById(R.id.tvPriceCategory);
            tvCategory = itemView.findViewById(R.id.tvFoodCategory);
            tvStars = itemView.findViewById(R.id.tvStars);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }

    private String getStarsString(int stars) {
        return "★".repeat(Math.max(0, stars));
    }
}
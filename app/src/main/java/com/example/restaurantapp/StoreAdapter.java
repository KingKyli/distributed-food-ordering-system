package com.example.restaurantapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantapp.ui.components.PrimaryButton;
import com.example.restaurantapp.ui.components.SecondaryButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private final Context context;
    private final List<Store> storeList;

    public StoreAdapter(Context context, List<Store> storeList) {
        this.context = context;
        this.storeList = storeList == null ? new ArrayList<>() : new ArrayList<>(storeList);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        Store store = storeList.get(position);
        String name = store != null ? store.getStoreName() : "";
        return name == null ? 0 : name.trim().toLowerCase(Locale.ROOT).hashCode();
    }

    public void updateStores(List<Store> newStores) {
        List<Store> next = newStores == null ? new ArrayList<>() : new ArrayList<>(newStores);
        List<Store> old = new ArrayList<>(storeList);

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return old.size();
            }

            @Override
            public int getNewListSize() {
                return next.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                Store o = old.get(oldItemPosition);
                Store n = next.get(newItemPosition);
                if (o == null || n == null) {
                    return o == n;
                }
                String on = o.getStoreName();
                String nn = n.getStoreName();
                return on != null && nn != null && on.equalsIgnoreCase(nn);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Store o = old.get(oldItemPosition);
                Store n = next.get(newItemPosition);
                if (o == null || n == null) {
                    return o == n;
                }
                return safe(o.getFoodCategory()).equals(safe(n.getFoodCategory()))
                        && safe(o.getPriceCategory()).equals(safe(n.getPriceCategory()))
                        && safeInt(o.getStoreStars()) == safeInt(n.getStoreStars());
            }
        });

        storeList.clear();
        storeList.addAll(next);
        diff.dispatchUpdatesTo(this);
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
        holder.tvCategory.setText(store.getFoodCategory());

        // Dynamic restaurant photo from spritesheet tiles
        if (holder.ivRestaurantIcon != null) {
            int imageRes = RestaurantImageHelper.getImageRes(store.getStoreName());
            holder.ivRestaurantIcon.setImageResource(imageRes);
            // When a real food photo is available, show it without padding/tint;
            // fall back to the tinted icon style if no photo exists.
            if (RestaurantImageHelper.hasImage(store.getStoreName())) {
                holder.ivRestaurantIcon.setPadding(0, 0, 0, 0);
                holder.ivRestaurantIcon.clearColorFilter();
            } else {
                int pad = (int) (18 * context.getResources().getDisplayMetrics().density);
                holder.ivRestaurantIcon.setPadding(pad, pad, pad, pad);
            }
        }

        // Rating: "⭐ 4.0 • 20-30 min" format
        Integer starsInt = store.getStoreStars();
        double stars = starsInt != null ? starsInt : 0;
        
        if (holder.tvRating != null) {
            holder.tvRating.setText(String.format(Locale.getDefault(), "⭐ %.1f • 20-30 min", stars));
        }

        // Favourites heart
        if (holder.ivHeart != null) {
            boolean fav = FavoritesRepository.isFavorite(context, store.getStoreName());
            holder.ivHeart.setImageResource(fav ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            holder.ivHeart.setOnClickListener(v -> {
                boolean nowFav = FavoritesRepository.toggleFavorite(context, store.getStoreName());
                holder.ivHeart.setImageResource(nowFav ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            });
        }

        // View Menu button - navigate to details
        if (holder.btnOpen != null) {
            holder.btnOpen.setOnClickListener(v -> {
                Intent intent = new Intent(context, RestaurantDetailsActivity.class);
                org.json.JSONObject storeJson = store.toJson();
                if (storeJson == null) return;
                intent.putExtra("store_json", storeJson.toString());
                context.startActivity(intent);
            });
        }

        // Order Now button - navigate to details (same for now)
        if (holder.btnOrder != null) {
            holder.btnOrder.setOnClickListener(v -> {
                Intent intent = new Intent(context, RestaurantDetailsActivity.class);
                org.json.JSONObject storeJson = store.toJson();
                if (storeJson == null) return;
                intent.putExtra("store_json", storeJson.toString());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvRating;
        SecondaryButton btnOpen;
        PrimaryButton btnOrder;
        ImageView ivHeart;
        ImageView ivRestaurantIcon;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName          = itemView.findViewById(R.id.tvRestaurantName);
            tvCategory      = itemView.findViewById(R.id.tvFoodCategory);
            tvRating        = itemView.findViewById(R.id.tvRating);
            btnOpen         = itemView.findViewById(R.id.btnOpen);
            btnOrder        = itemView.findViewById(R.id.btnOrder);
            ivHeart         = itemView.findViewById(R.id.ivHeart);
            ivRestaurantIcon = itemView.findViewById(R.id.ivRestaurantIcon);
            
            // Set button text
            if (btnOpen != null) {
                btnOpen.setText("View Menu");
                btnOpen.setShowIcon(true);
            }
            if (btnOrder != null) {
                btnOrder.setText("Order now");
            }
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

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
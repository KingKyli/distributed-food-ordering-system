package com.example.restaurantapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.BasketViewHolder> {
    private List<BasketItem> items = new ArrayList<>();
    private final BasketActivity basketActivity;

    public BasketAdapter(BasketActivity activity) {
        this.basketActivity = activity;
        items.addAll(Basket.getInstance().getItems());
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        BasketItem item = items.get(position);
        return item == null ? 0 : item.getStableId().hashCode();
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
        BasketItem item = items.get(position);
        int quantity = item.getQuantity();
        holder.tvName.setText(item.getProductName());
        holder.tvQuantity.setText("x" + quantity);
        holder.tvPrice.setText(String.format(Locale.getDefault(), "€%.2f", item.getSubtotal()));
        holder.tvQuantityField.setText(String.valueOf(quantity));
        holder.ivPlus.setOnClickListener(v -> {
            Product product = new Product(item.getProductName(), item.getProductType(), 1, item.getUnitPrice());
            Basket.getInstance().addProduct(product, item.getStoreName());
            basketActivity.notifyBasketChanged();
        });
        holder.ivMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                Basket.getInstance().removeProductById(item.getStableId());
                basketActivity.notifyBasketChanged();
            } else {
                // Snapshot before removal for undo
                String removedName = item.getProductName();
                String removedType = item.getProductType();
                double removedPrice = item.getUnitPrice();
                String removedStore = item.getStoreName();

                Basket.getInstance().removeProductById(item.getStableId());
                basketActivity.notifyBasketChanged();

                View anchor = basketActivity.findViewById(android.R.id.content);
                Snackbar.make(anchor,
                        basketActivity.getString(R.string.basket_item_removed),
                        Snackbar.LENGTH_LONG)
                        .setAction(basketActivity.getString(R.string.common_undo), u -> {
                            Product restored = new Product(removedName, removedType, 1, removedPrice);
                            Basket.getInstance().addProduct(restored, removedStore);
                            basketActivity.notifyBasketChanged();
                        })
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems() {
        List<BasketItem> next = new ArrayList<>(Basket.getInstance().getItems());
        List<BasketItem> old = new ArrayList<>(items);

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
                BasketItem o = old.get(oldItemPosition);
                BasketItem n = next.get(newItemPosition);
                if (o == null || n == null) {
                    return o == n;
                }
                return o.getStableId().equals(n.getStableId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                BasketItem o = old.get(oldItemPosition);
                BasketItem n = next.get(newItemPosition);
                if (o == null || n == null) {
                    return o == n;
                }
                return o.getQuantity() == n.getQuantity()
                        && Double.compare(o.getUnitPrice(), n.getUnitPrice()) == 0;
            }
        });

        items.clear();
        items.addAll(next);
        diff.dispatchUpdatesTo(this);
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

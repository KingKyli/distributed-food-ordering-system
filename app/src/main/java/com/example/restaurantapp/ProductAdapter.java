package com.example.restaurantapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> products;
    private final String storeName;

    public ProductAdapter(List<Product> products, String storeName) {
        this.products = products;
        this.storeName = storeName;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        Product p = products.get(position);
        if (p == null) return 0;
        return p.getStableKey().hashCode();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detail_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        if (product == null) return;

        holder.tvProductName.setText(product.getProductName());
        holder.tvProductType.setText(product.getProductType());
        holder.tvProductPrice.setText(String.format(Locale.getDefault(), "€%.2f", product.getPrice()));

        // Show stock badge
        int stock = product.getAvailableAmount();
        if (holder.tvProductStock != null) {
            if (stock <= 0) {
                holder.tvProductStock.setText("Out of stock");
                holder.tvProductStock.setTextColor(
                        ContextCompat.getColor(holder.itemView.getContext(), R.color.text_error));
                holder.ivProductCart.setAlpha(0.4f);
                holder.ivProductCart.setEnabled(false);
            } else {
                holder.tvProductStock.setText(stock + " left");
                holder.tvProductStock.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                        stock <= 3 ? R.color.text_warning : R.color.text_success));
                holder.ivProductCart.setAlpha(1f);
                holder.ivProductCart.setEnabled(true);
            }
        }

        holder.ivProductCart.setOnClickListener(v -> {
            boolean added = Basket.getInstance().addProduct(product, storeName);
            if (!added) {
                Toast.makeText(holder.itemView.getContext(),
                        "You can only add items from one store at a time.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(holder.itemView.getContext(),
                        product.getProductName() + " added to basket ✓", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return products == null ? 0 : products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductType, tvProductPrice, tvProductStock;
        ImageView ivProductCart;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName  = itemView.findViewById(R.id.tvProductName);
            tvProductType  = itemView.findViewById(R.id.tvProductType);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductStock = itemView.findViewById(R.id.tvProductStock);
            ivProductCart  = itemView.findViewById(R.id.ivProductCart);
        }
    }
}

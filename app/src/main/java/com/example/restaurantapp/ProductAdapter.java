package com.example.restaurantapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/**
 * Displays a list of {@link Product} objects on the restaurant detail screen.
 * Operates entirely on typed model objects — no raw JSON parsing in the UI layer.
 */
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
        Product product = products.get(position);
        return product == null ? 0 : product.getStableKey().hashCode();
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
        holder.tvProductName.setText(product.getProductName());
        holder.tvProductType.setText(product.getProductType());
        holder.tvProductPrice.setText(
                String.format(Locale.getDefault(), "\u20ac%.2f", product.getPrice()));
        holder.ivProductCart.setOnClickListener(v -> {
            boolean added = Basket.getInstance().addProduct(product, storeName);
            String msg = added
                    ? holder.itemView.getContext().getString(R.string.product_added_to_basket)
                    : holder.itemView.getContext().getString(R.string.product_single_store_warning);
            Toast.makeText(holder.itemView.getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductType, tvProductPrice;
        ImageView ivProductCart;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductType = itemView.findViewById(R.id.tvProductType);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            ivProductCart = itemView.findViewById(R.id.ivProductCart);
        }
    }
}

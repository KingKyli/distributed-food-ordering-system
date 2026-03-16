package com.example.restaurantapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView; // <-- Add this import

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.util.List;
import org.json.JSONException;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private final List<JSONObject> products;
    private final String storeName;
    public ProductAdapter(List<JSONObject> products, String storeName) {
        this.products = products;
        this.storeName = storeName;
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
        JSONObject productJson = products.get(position);
        try {
            holder.tvProductName.setText(productJson.getString("ProductName"));
            holder.tvProductType.setText(productJson.getString("ProductType"));
            holder.tvProductPrice.setText("€" + productJson.getDouble("Price"));

            holder.ivProductCart.setOnClickListener(v -> {
                Product product = null;
                try {
                    product = Product.fromJson(productJson);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                boolean added = Basket.getInstance().addProduct(product, storeName);
                if (!added) {
                    Toast.makeText(holder.itemView.getContext(), "You can only add products from one store at a time.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Added to basket", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            holder.tvProductName.setText("-");
            holder.tvProductType.setText("");
            holder.tvProductPrice.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductType, tvProductPrice;
        ImageView ivProductCart; // <-- Make sure this is ImageView
        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductType = itemView.findViewById(R.id.tvProductType);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            ivProductCart = itemView.findViewById(R.id.ivProductCart);
        }
    }
}

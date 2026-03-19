package com.example.restaurantapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // For color resources
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditProductActivity extends AppCompatActivity {
    private static final String TAG = "EditProductActivity";

    private ProductAdapter adapter;
    private Store currentStore;
    private volatile boolean activityActive;
    private final ProductManagementService productManagementService = new ProductManagementService();
    private final NumberFormat priceFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

    // It's good practice to define Intent keys as constants
    public static final String EXTRA_STORE_JSON = "store_json";
    public static final String EXTRA_PRODUCT_INDEX = "product_index";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ActivityUtils.ensureConnectedOrRedirect(this)) {
            return;
        }

        setContentView(R.layout.activity_product_list);
        activityActive = true;

        RecyclerView recyclerView = findViewById(R.id.recyclerProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String storeJson = getIntent().getStringExtra(EXTRA_STORE_JSON);
        if (storeJson == null || storeJson.trim().isEmpty()) {
            storeJson = PartnerSessionStore.getStoreJson(this);
        }

        if (storeJson != null) {
            try {
                JSONObject jsonObj = new JSONObject(storeJson);
                Store parsedStore = Store.fromJson(jsonObj);
                currentStore = parsedStore;
                List<Product> parsedProducts = parsedStore.getProducts();
                adapter = new ProductAdapter(parsedProducts != null ? parsedProducts : new ArrayList<>());
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse store JSON for product editing.", e);
                adapter = new ProductAdapter(new ArrayList<>());
            }
        } else {
            adapter = new ProductAdapter(new ArrayList<>());
        }

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProductListFromServer();
    }

    @Override
    protected void onDestroy() {
        activityActive = false;
        super.onDestroy();
    }


    private void refreshProductListFromServer() {
        if (currentStore == null) return;
        String storeName = currentStore.getStoreName();
        new Thread(() -> {
            AppResult<Store> result = productManagementService.refreshStore(storeName);
            ActivityUtils.runOnUiThreadIfAlive(this, () -> {
                if (!activityActive) {
                    return;
                }
                if (!result.isSuccess()) {
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                currentStore = result.getData();
                adapter.replaceProducts(currentStore.getProducts());
            });
        }).start();
    }

    private class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private final List<Product> products;

        ProductAdapter(List<Product> products) {
            this.products = products;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            Product product = products.get(position);
            holder.bind(product);
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        void replaceProducts(List<Product> updatedProducts) {
            List<Product> safeUpdatedProducts = updatedProducts == null
                    ? new ArrayList<>()
                    : new ArrayList<>(updatedProducts);
            List<Product> oldProducts = new ArrayList<>(products);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return oldProducts.size();
                }

                @Override
                public int getNewListSize() {
                    return safeUpdatedProducts.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return oldProducts.get(oldItemPosition).getStableKey()
                            .equals(safeUpdatedProducts.get(newItemPosition).getStableKey());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Product oldProduct = oldProducts.get(oldItemPosition);
                    Product newProduct = safeUpdatedProducts.get(newItemPosition);
                    return oldProduct.getAvailableAmount() == newProduct.getAvailableAmount()
                            && Double.compare(oldProduct.getPrice(), newProduct.getPrice()) == 0
                            && oldProduct.isActive() == newProduct.isActive()
                            && TextUtils.equals(oldProduct.getProductName(), newProduct.getProductName())
                            && TextUtils.equals(oldProduct.getProductType(), newProduct.getProductType());
                }
            });

            products.clear();
            products.addAll(safeUpdatedProducts);
            diffResult.dispatchUpdatesTo(this);
        }

        class ProductViewHolder extends RecyclerView.ViewHolder {
            TextView txtName, txtType, txtStatus, txtPrice, txtStock;
            ImageButton btnEdit, btnDelete;

            ProductViewHolder(View itemView) {
                super(itemView);
                txtName = itemView.findViewById(R.id.textProductName);
                txtType = itemView.findViewById(R.id.textProductType);
                txtPrice = itemView.findViewById(R.id.textPrice);
                txtStock = itemView.findViewById(R.id.textStock);
                txtStatus = itemView.findViewById(R.id.textStatus);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }

            void bind(Product product) {
                txtName.setText(product.getProductName());
                txtType.setText(TextUtils.isEmpty(product.getProductType())
                        ? itemView.getContext().getString(R.string.product_type_fallback)
                        : product.getProductType());
                txtPrice.setText(priceFormatter.format(product.getPrice()));
                txtStock.setText(itemView.getContext().getString(R.string.product_stock_format, product.getAvailableAmount()));

                if (product.isActive()) {
                    txtName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                    txtType.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
                    txtPrice.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));
                    txtStock.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));

                    txtStatus.setText(R.string.product_status_active);
                    txtStatus.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.status_badge_active_background));
                    txtStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_active_text));

                    btnEdit.setEnabled(true);
                    btnEdit.setAlpha(1.0f);
                    btnDelete.setEnabled(true);
                    btnDelete.setAlpha(1.0f);

                    itemView.setAlpha(1.0f);
                    itemView.setClickable(true);
                } else {
                    txtName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.disabled_text_color));
                    txtType.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.disabled_text_color));
                    txtPrice.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.disabled_text_color));
                    txtStock.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.disabled_text_color));

                    txtStatus.setText(R.string.product_status_inactive);
                    txtStatus.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.status_badge_inactive_background));
                    txtStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_inactive_text));

                    btnEdit.setEnabled(false);
                    btnEdit.setAlpha(0.45f);
                    btnDelete.setEnabled(false);
                    btnDelete.setAlpha(0.45f);

                    itemView.setAlpha(0.72f);
                }

                btnEdit.setOnClickListener(v -> {
                    if (product.isActive()) {
                        int currentPosition = getAdapterPosition();
                        if (currentPosition == RecyclerView.NO_POSITION) {
                            return;
                        }
                        Intent intent = new Intent(EditProductActivity.this, ProductEditActivity.class);
                        intent.putExtra(EXTRA_PRODUCT_INDEX, currentPosition);
                        if (currentStore != null && currentStore.toJson() != null) {
                            intent.putExtra(EXTRA_STORE_JSON, currentStore.toJson().toString());
                        } else {
                            return;
                        }
                        try {
                            intent.putExtra("product_json", product.toJson().toString());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        startActivity(intent);
                    }
                });

                btnDelete.setOnClickListener(v -> {
                    if (product.isActive()) {
                        new AlertDialog.Builder(EditProductActivity.this)
                                .setTitle(R.string.product_delete_confirm_title)
                                .setMessage(getString(R.string.product_delete_confirm_message, product.getProductName()))
                                .setPositiveButton(R.string.common_delete, (dialog, which) -> {
                                    int currentPosition = getAdapterPosition();
                                    if (currentPosition != RecyclerView.NO_POSITION) {
                                        Product toRemove = products.get(currentPosition);
                                        products.remove(currentPosition);
                                        notifyItemRemoved(currentPosition);
                                        // Send delete request to server
                                        String storeName = (currentStore != null) ? currentStore.getStoreName() : null;
                                        String productName = toRemove.getProductName();
                                        if (storeName != null && productName != null) {
                                            new Thread(() -> {
                                                AppResult<Void> result = productManagementService.removeProduct(storeName, productName);
                                                ActivityUtils.runOnUiThreadIfAlive(EditProductActivity.this, () -> {
                                                    if (!activityActive) {
                                                        return;
                                                    }
                                                    if (result.isSuccess()) {
                                                        Toast.makeText(EditProductActivity.this, R.string.product_deleted_success, Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(EditProductActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }).start();
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.common_cancel, null)
                                .show();
                    }
                });
            }
        }
    }
}


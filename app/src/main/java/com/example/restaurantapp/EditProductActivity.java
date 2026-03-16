package com.example.restaurantapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color; // Import for color
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // For color resources
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditProductActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private Store currentStore;

    // It's good practice to define Intent keys as constants
    public static final String EXTRA_STORE_JSON = "store_json";
    public static final String EXTRA_PRODUCT_INDEX = "product_index";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String storeJson = getIntent().getStringExtra(EXTRA_STORE_JSON);

        if (storeJson != null) {
            try {
                JSONObject jsonObj = new JSONObject(storeJson);
                currentStore = Store.fromJson(jsonObj); // Assumes Store.fromJson exists and works
                if (currentStore != null) {
                    productList = currentStore.getProducts(); // Assumes getProducts returns a mutable list if you intend to modify it directly
                } else {
                    // Handle case where Store.fromJson might return null
                    productList = new ArrayList<>();
                    // Log error or show message
                }
            } catch (JSONException e) {
                // Log this error properly in a real app
                e.printStackTrace();
                productList = new ArrayList<>();
                // Optionally show an error message to the user
            }
        } else {
            productList = new ArrayList<>();
            // Optionally show a message if no store data is provided
        }

        if (productList == null) { // Ensure productList is never null
            productList = new ArrayList<>();
        }

        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProductListFromServer();
    }


    private void refreshProductListFromServer() {
        if (currentStore == null) return;
        String storeName = currentStore.getStoreName();
        new Thread(() -> {
            MasterCommunicator comm = ServerConnection.getInstance();
            // Στείλε ΚΕΝΕΣ τιμές για να πάρεις ΟΛΑ τα καταστήματα
            String result = comm.sendSearchRequest("", "", "", "", "");
            runOnUiThread(() -> {
                try {
                    org.json.JSONArray arr = new org.json.JSONArray(result);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        if (obj.getString("StoreName").equalsIgnoreCase(storeName)) {
                            Store store = Store.fromJson(obj);
                            productList.clear();
                            productList.addAll(store.getProducts());
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    private class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private List<Product> products;

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

        class ProductViewHolder extends RecyclerView.ViewHolder {
            TextView txtName, txtStatus, txtPrice, txtStock;
            ImageButton btnEdit, btnDelete;

            ProductViewHolder(View itemView) {
                super(itemView);
                txtName = itemView.findViewById(R.id.textProductName);
                txtPrice = itemView.findViewById(R.id.textPrice);
                txtStock = itemView.findViewById(R.id.textStock);
                txtStatus = itemView.findViewById(R.id.textStatus);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }

            void bind(Product product) {
                // Basic product info
                txtName.setText(product.getProductName());
                txtPrice.setText("$" + product.getPrice()); // Consider using String.format for currency
                txtStock.setText("Stock: " + product.getAvailableAmount());

                // --- New logic for active/inactive state ---
                // Assuming Product class has an isActive() method
                if (product.isActive()) {
                    txtName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.primary_text_color)); // Define in colors.xml
                    txtPrice.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.secondary_text_color)); // Define in colors.xml
                    txtStock.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.secondary_text_color)); // Define in colors.xml

                    txtStatus.setText("Active"); // Or "In Stock", "Available"
                    txtStatus.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.status_active_background)); // Define in colors.xml
                    txtStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_active_text)); // Define in colors.xml

                    btnEdit.setEnabled(true);
                    btnEdit.setAlpha(1.0f);
                    btnDelete.setEnabled(true);
                    btnDelete.setAlpha(1.0f);

                    itemView.setAlpha(1.0f); // Ensure item is not grayed out
                    itemView.setClickable(true); // Ensure whole item is clickable if needed

                } else { // Product is not active
                    txtName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.disabled_text_color)); // Define in colors.xml
                    txtPrice.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.disabled_text_color));
                    txtStock.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.disabled_text_color));

                    txtStatus.setText("Inactive"); // Or "Out of Stock", "Unavailable"
                    txtStatus.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.status_inactive_background)); // Define in colors.xml
                    txtStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_inactive_text)); // Define in colors.xml

                    btnEdit.setEnabled(false);
                    btnEdit.setAlpha(0.5f); // Visual cue for disabled
                    btnDelete.setEnabled(false);
                    btnDelete.setAlpha(0.5f); // Visual cue for disabled

                    itemView.setAlpha(0.6f); // Gray out the entire item slightly
                    // itemView.setClickable(false); // Optionally make the whole item non-clickable
                }
                // --- End of new logic ---

                btnEdit.setOnClickListener(v -> {
                    // Check if product is active before launching edit, though setEnabled should handle it
                    if (product.isActive()) {
                        Intent intent = new Intent(EditProductActivity.this, ProductEditActivity.class);
                        intent.putExtra(EXTRA_PRODUCT_INDEX, getAdapterPosition());
                        // Ensure currentStore and toJson() are not null and handle potential errors
                        if (currentStore != null && currentStore.toJson() != null) {
                            intent.putExtra(EXTRA_STORE_JSON, currentStore.toJson().toString());
                        } else {
                            // Handle error: currentStore or its JSON representation is null
                            // Maybe show a toast or log
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
                    // Check if product is active before allowing deletion, though setEnabled should handle it
                    if (product.isActive()) {
                        new AlertDialog.Builder(EditProductActivity.this)
                                .setTitle("Confirm Deletion") // Extract to strings.xml
                                .setMessage("Are you sure you want to delete this product '" + product.getProductName() + "'?") // Extract and format
                                .setPositiveButton("Yes", (dialog, which) -> { // Extract to strings.xml
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
                                                MasterCommunicator comm = ServerConnection.getInstance();
                                                boolean success = comm.sendRemoveProductRequest(storeName, productName);
                                                runOnUiThread(() -> {
                                                    if (success) {
                                                        Toast.makeText(EditProductActivity.this, "Product deleted from server", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(EditProductActivity.this, "Failed to delete product from server", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }).start();
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", null) // Extract to strings.xml
                                .show();
                    }
                });
            }
        }
    }
}


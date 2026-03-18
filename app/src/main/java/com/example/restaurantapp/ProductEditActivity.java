package com.example.restaurantapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductEditActivity extends AppCompatActivity {

    private EditText inputName, inputType, inputPrice, inputQuantity;
    private CheckBox checkboxActive;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);

        inputName = findViewById(R.id.inputName);
        inputType = findViewById(R.id.inputType);
        inputPrice = findViewById(R.id.inputPrice);
        inputQuantity = findViewById(R.id.inputQuantity);
        checkboxActive = findViewById(R.id.checkboxActive);
        Button btnSave = findViewById(R.id.btnSave);

        String productJson = getIntent().getStringExtra("product_json");
        if (productJson != null) {
            try {
                JSONObject obj = new JSONObject(productJson);
                product = Product.fromJson(obj);

                inputName.setText(product.getProductName());
                inputType.setText(product.getProductType());
                inputPrice.setText(String.valueOf(product.getPrice()));
                inputQuantity.setText(String.valueOf(product.getAvailableAmount()));
                checkboxActive.setChecked(product.isActive());

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load product data", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        String storeJson = getIntent().getStringExtra("store_json");
        String storeName = null;
        if (storeJson != null) {
            try {
                JSONObject obj = new JSONObject(storeJson);
                storeName = obj.getString("StoreName");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        final String finalStoreName = storeName;
        final Product finalProduct = product;
        btnSave.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String type = inputType.getText().toString().trim();
            String priceStr = inputPrice.getText().toString().trim();
            String quantityStr = inputQuantity.getText().toString().trim();
            boolean isActive = checkboxActive.isChecked();

            if (name.isEmpty() || type.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int quantity = Integer.parseInt(quantityStr);

                finalProduct.setAvailableAmount(quantity);
                finalProduct.setPrice(price);
                finalProduct.setActive(isActive);
                // Optionally update name/type if allowed

                // Send update to server
                if (finalStoreName != null && finalProduct != null) {
                    String productName = finalProduct.getProductName();
                    MasterCommunicator comm = ConnectionUtils.requireConnected(ProductEditActivity.this);
                    if (comm == null) return;
                    new Thread(() -> {
                        boolean success = comm.sendUpdateProductRequest(finalStoreName, productName, price, quantity);
                        runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(this, "Product updated on server", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to update product on server", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                } else {
                    Toast.makeText(this, "Store info missing", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price or quantity", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

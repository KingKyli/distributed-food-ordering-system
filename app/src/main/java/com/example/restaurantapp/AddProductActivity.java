package com.example.restaurantapp;

import android.os.Bundle;
import com.google.android.material.button.MaterialButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddProductActivity extends AppCompatActivity {

    private EditText inputName, inputType, inputPrice, inputQuantity;
    private MaterialButton btnAddProduct;
    private volatile boolean activityActive;
    private final ProductManagementService productManagementService = new ProductManagementService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ActivityUtils.ensureConnectedOrRedirect(this)) {
            return;
        }

        setContentView(R.layout.activity_manage_products);  // Make sure this XML exists
        activityActive = true;

        inputName = findViewById(R.id.inputName);
        inputType = findViewById(R.id.inputType);
        inputPrice = findViewById(R.id.inputPrice);
        inputQuantity = findViewById(R.id.inputQuantity);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        // Get store JSON from intent
        String storeJson = getIntent().getStringExtra("store_json");
        if (storeJson == null || storeJson.trim().isEmpty()) {
            storeJson = PartnerSessionStore.getStoreJson(this);
        }
        final String storeName = StoreJsonUtils.extractStoreName(storeJson);

        btnAddProduct.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                String name = inputName.getText().toString().trim();
                String type = inputType.getText().toString().trim();
                String priceStr = inputPrice.getText().toString().trim();
                String quantityStr = inputQuantity.getText().toString().trim();

                if (name.isEmpty() || type.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
                    Toast.makeText(AddProductActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double price = Double.parseDouble(priceStr);
                    int quantity = Integer.parseInt(quantityStr);
                    Product newProduct = new Product(name, type, quantity, price);

                    if (storeName == null || storeName.trim().isEmpty()) {
                        Toast.makeText(AddProductActivity.this, "Store info missing", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        AppResult<Void> result = productManagementService.addProduct(storeName, newProduct);
                        ActivityUtils.runOnUiThreadIfAlive(AddProductActivity.this, () -> {
                            if (!activityActive) {
                                return;
                            }
                            if (result.isSuccess()) {
                                Toast.makeText(AddProductActivity.this, "Product added to server: " + name, Toast.LENGTH_SHORT).show();
                                inputName.setText("");
                                inputType.setText("");
                                inputPrice.setText("");
                                inputQuantity.setText("");
                            } else {
                                Toast.makeText(AddProductActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                } catch (NumberFormatException e) {
                    Toast.makeText(AddProductActivity.this, "Invalid number format", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        activityActive = false;
        super.onDestroy();
    }
}



package com.example.restaurantapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddProductActivity extends AppCompatActivity {

    private EditText inputName, inputType, inputPrice, inputQuantity;
    private Button btnAddProduct;
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
        final String storeName;
        String storeJson = getIntent().getStringExtra("store_json");
        if (storeJson == null || storeJson.trim().isEmpty()) {
            storeJson = PartnerSessionStore.getStoreJson(this);
        }
        if (storeJson != null) {
            String tempName = null;
            try {
                org.json.JSONObject obj = new org.json.JSONObject(storeJson);
                tempName = obj.getString("StoreName");
            } catch (Exception e) {
                Toast.makeText(this, "Error reading store info", Toast.LENGTH_SHORT).show();
            }
            storeName = tempName;
        } else {
            storeName = null;
        }

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

                    if (storeName != null) {
                        MasterCommunicator comm = ServerConnection.getInstance();
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean success = comm.sendAddProductRequest(storeName, newProduct);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (success) {
                                            Toast.makeText(AddProductActivity.this, "Product added to server: " + name, Toast.LENGTH_SHORT).show();
                                            inputName.setText("");
                                            inputType.setText("");
                                            inputPrice.setText("");
                                            inputQuantity.setText("");
                                        } else {
                                            Toast.makeText(AddProductActivity.this, "Failed to add product to server", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                        t.start();
                    } else {
                        Toast.makeText(AddProductActivity.this, "Store info missing", Toast.LENGTH_SHORT).show();
                    }
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


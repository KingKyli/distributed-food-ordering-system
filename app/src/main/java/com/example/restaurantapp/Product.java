package com.example.restaurantapp;

import org.json.JSONObject;
import org.json.JSONException;

class Product {
    private String productName;
    private String productType;
    private int availableAmount;
    private double price;
    private boolean isActive;
    private int unitsSold;
    private double revenueFromThisProduct;

    private boolean active;

    // Constructor
    public Product(String productName, String productType, int availableAmount, double price) {
        this.productName = productName;
        this.productType = productType;
        this.availableAmount = availableAmount;
        this.price = price;
        this.isActive = true;
        this.unitsSold = 0;
        this.revenueFromThisProduct = 0.0;
    }

    // Getters
    public String getProductName() { return productName; }
    public String getProductType() { return productType; }
    public int getAvailableAmount() { return availableAmount; }
    public double getPrice() { return price; }
    public boolean isActive() { return isActive; }
    public int getUnitsSold() { return unitsSold; }
    public double getRevenueFromThisProduct() { return revenueFromThisProduct; }

    public void setAvailableAmount(int availableAmount) {
        this.availableAmount = availableAmount;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public void setActive(boolean active) {
        isActive = active;
    }

    public void recordSale(int quantity) {
        if (quantity > 0 && this.availableAmount >= quantity) {
            this.availableAmount -= quantity;
            this.unitsSold += quantity;
            this.revenueFromThisProduct += quantity * this.price;
        } else {
            System.err.println("Error recording sale for " + productName + ": Not enough stock or invalid quantity.");
        }
    }

    public static Product fromJson(JSONObject obj) throws JSONException {
        String name = obj.getString("ProductName");
        String type = obj.optString("ProductType", "Unknown");
        int amount = obj.optInt("AvailableAmount", 0);
        double price = obj.getDouble("Price");

        return new Product(name, type, amount, price);
    }



    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("ProductName", this.productName);
        json.put("ProductType", this.productType);
        json.put("AvailableAmount", this.availableAmount);
        json.put("Price", this.price);
        return json;
    }


    @Override
    public String toString() {
        return "Product{" +
                "name='" + productName + '\'' +
                ", type='" + productType + '\'' +
                ", amount=" + availableAmount +
                ", price=" + price +
                ", active=" + isActive +
                ", sold=" + unitsSold +
                '}';
    }
}
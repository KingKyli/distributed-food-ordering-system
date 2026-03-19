package com.example.restaurantapp;

public final class BasketItem {
    private final String storeName;
    private final String productName;
    private final String productType;
    private int quantity;
    private double unitPrice;

    public BasketItem(String storeName, String productName, String productType, int quantity, double unitPrice) {
        this.storeName = storeName;
        this.productName = productName;
        this.productType = productType;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductType() {
        return productType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getSubtotal() {
        return unitPrice * quantity;
    }

    public String getStableId() {
        return buildStableId(storeName, productName, productType);
    }

    public static String buildStableId(String storeName, String productName, String productType) {
        return normalize(storeName) + "::" + normalize(productName) + "::" + normalize(productType);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}


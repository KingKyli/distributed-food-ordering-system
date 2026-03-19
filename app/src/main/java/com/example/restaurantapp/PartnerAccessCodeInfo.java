package com.example.restaurantapp;

public class PartnerAccessCodeInfo {
    private final String storeName;
    private final String deliveryDestination;
    private final String demoCode;
    private final int expiresInMinutes;

    public PartnerAccessCodeInfo(String storeName, String deliveryDestination, String demoCode, int expiresInMinutes) {
        this.storeName = storeName;
        this.deliveryDestination = deliveryDestination;
        this.demoCode = demoCode;
        this.expiresInMinutes = expiresInMinutes;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getDeliveryDestination() {
        return deliveryDestination;
    }

    public String getDemoCode() {
        return demoCode;
    }

    public int getExpiresInMinutes() {
        return expiresInMinutes;
    }
}


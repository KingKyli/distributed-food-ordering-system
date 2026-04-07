package com.example.restaurantapp;

public interface ServerGateway {
    AppResult<String> search(String latitude, String longitude, String foodCategory, String stars, String priceRange);

    AppResult<String> partnerLogin(String storeName, String accessCode);

    AppResult<String> requestPartnerAccessCode(String storeName);

    AppResult<String> buy(String storeName, String productName, int quantity);

    AppResult<String> addProduct(String storeName, Product product);

    AppResult<String> updateProduct(String storeName, String productName, double newPrice, int newAmount);

    AppResult<String> removeProduct(String storeName, String productName);
}
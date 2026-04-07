package com.example.restaurantapp;

import java.util.Collections;
import java.util.List;

public class StubRestaurantRepository extends RestaurantRepository {
    AppResult<List<Store>> searchResult = AppResult.success(Collections.emptyList());
    AppResult<Store> storeByNameResult = AppResult.error("Store not configured");
    AppResult<List<Product>> storeProductsResult = AppResult.success(Collections.emptyList());

    StubRestaurantRepository() {
        super(new FakeServerGateway());
    }

    @Override
    public AppResult<List<Store>> searchStores(String latitude, String longitude, String cuisine, int stars, String price) {
        return searchResult;
    }

    @Override
    public AppResult<Store> fetchStoreByName(String storeName) {
        return storeByNameResult;
    }

    @Override
    public AppResult<List<Product>> fetchStoreProducts(String storeName) {
        return storeProductsResult;
    }
}
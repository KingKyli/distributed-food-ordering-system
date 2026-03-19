package com.example.restaurantapp;

import java.util.List;

public class ProductManagementService {
    private final RestaurantRepository restaurantRepository = new RestaurantRepository();

    public AppResult<Void> addProduct(String storeName, Product product) {
        if (storeName == null || storeName.trim().isEmpty()) {
            return AppResult.error("Store info missing");
        }
        if (product == null) {
            return AppResult.error("Product details are missing");
        }
        MasterCommunicator communicator = ServerConnection.getInstance();
        if (communicator == null) {
            return AppResult.error("Server connection is not available");
        }
        return communicator.sendAddProductRequest(storeName, product)
                ? AppResult.success(null)
                : AppResult.error("Failed to add product to server");
    }

    public AppResult<Void> updateProduct(String storeName, String productName, double price, int quantity) {
        MasterCommunicator communicator = ServerConnection.getInstance();
        if (communicator == null) {
            return AppResult.error("Server connection is not available");
        }
        return communicator.sendUpdateProductRequest(storeName, productName, price, quantity)
                ? AppResult.success(null)
                : AppResult.error("Failed to update product on server");
    }

    public AppResult<Void> removeProduct(String storeName, String productName) {
        MasterCommunicator communicator = ServerConnection.getInstance();
        if (communicator == null) {
            return AppResult.error("Server connection is not available");
        }
        return communicator.sendRemoveProductRequest(storeName, productName)
                ? AppResult.success(null)
                : AppResult.error("Failed to delete product from server");
    }

    public AppResult<Store> refreshStore(String storeName) {
        return restaurantRepository.fetchStoreByName(storeName);
    }

    public AppResult<List<Product>> fetchProducts(String storeName) {
        return restaurantRepository.fetchStoreProducts(storeName);
    }
}


package com.example.restaurantapp;

import java.util.List;

public class ProductManagementService {
    private final RestaurantRepository restaurantRepository;
    private final ServerGateway serverGateway;

    public ProductManagementService() {
        this(new RestaurantRepository(), new TcpServerGateway());
    }

    ProductManagementService(RestaurantRepository restaurantRepository, ServerGateway serverGateway) {
        this.restaurantRepository = restaurantRepository;
        this.serverGateway = serverGateway;
    }

    public AppResult<Void> addProduct(String storeName, Product product) {
        if (storeName == null || storeName.trim().isEmpty()) {
            return AppResult.error("Store info missing");
        }
        if (product == null) {
            return AppResult.error("Product details are missing");
        }
        AppResult<String> responseResult = serverGateway.addProduct(storeName, product);
        if (!responseResult.isSuccess()) {
            return AppResult.error(responseResult.getMessage());
        }
        String response = responseResult.getData();
        if (ProtocolUtils.isOkResponse(response)) {
            return AppResult.success(null);
        }
        return AppResult.error(ProtocolUtils.extractErrorMessage(response, "Failed to add product to server"));
    }

    public AppResult<Void> updateProduct(String storeName, String productName, double price, int quantity) {
        AppResult<String> responseResult = serverGateway.updateProduct(storeName, productName, price, quantity);
        if (!responseResult.isSuccess()) {
            return AppResult.error(responseResult.getMessage());
        }
        String response = responseResult.getData();
        if (ProtocolUtils.isOkResponse(response)) {
            return AppResult.success(null);
        }
        return AppResult.error(ProtocolUtils.extractErrorMessage(response, "Failed to update product on server"));
    }

    public AppResult<Void> removeProduct(String storeName, String productName) {
        AppResult<String> responseResult = serverGateway.removeProduct(storeName, productName);
        if (!responseResult.isSuccess()) {
            return AppResult.error(responseResult.getMessage());
        }
        String response = responseResult.getData();
        if (ProtocolUtils.isOkResponse(response)) {
            return AppResult.success(null);
        }
        return AppResult.error(ProtocolUtils.extractErrorMessage(response, "Failed to delete product from server"));
    }

    public AppResult<Store> refreshStore(String storeName) {
        return restaurantRepository.fetchStoreByName(storeName);
    }

    public AppResult<List<Product>> fetchProducts(String storeName) {
        return restaurantRepository.fetchStoreProducts(storeName);
    }
}


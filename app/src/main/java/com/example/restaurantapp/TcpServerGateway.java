package com.example.restaurantapp;

public class TcpServerGateway implements ServerGateway {

    private AppResult<MasterCommunicator> requireCommunicator() {
        return ServerConnection.requireCommunicator();
    }

    @Override
    public AppResult<String> search(String latitude, String longitude, String foodCategory, String stars, String priceRange) {
        AppResult<MasterCommunicator> communicatorResult = requireCommunicator();
        if (!communicatorResult.isSuccess()) {
            return AppResult.error(communicatorResult.getMessage());
        }
        return AppResult.success(
                communicatorResult.getData().sendSearchRequest(latitude, longitude, foodCategory, stars, priceRange)
        );
    }

    @Override
    public AppResult<String> partnerLogin(String storeName, String accessCode) {
        AppResult<MasterCommunicator> communicatorResult = requireCommunicator();
        if (!communicatorResult.isSuccess()) {
            return AppResult.error(communicatorResult.getMessage());
        }
        return AppResult.success(communicatorResult.getData().sendPartnerLoginRequestDetailed(storeName, accessCode));
    }

    @Override
    public AppResult<String> requestPartnerAccessCode(String storeName) {
        AppResult<MasterCommunicator> communicatorResult = requireCommunicator();
        if (!communicatorResult.isSuccess()) {
            return AppResult.error(communicatorResult.getMessage());
        }
        return AppResult.success(communicatorResult.getData().requestPartnerAccessCode(storeName));
    }

    @Override
    public AppResult<String> buy(String storeName, String productName, int quantity) {
        AppResult<MasterCommunicator> communicatorResult = requireCommunicator();
        if (!communicatorResult.isSuccess()) {
            return AppResult.error(communicatorResult.getMessage());
        }
        return AppResult.success(communicatorResult.getData().sendBuyRequestDetailed(storeName, productName, quantity));
    }

    @Override
    public AppResult<String> addProduct(String storeName, Product product) {
        AppResult<MasterCommunicator> communicatorResult = requireCommunicator();
        if (!communicatorResult.isSuccess()) {
            return AppResult.error(communicatorResult.getMessage());
        }
        return AppResult.success(communicatorResult.getData().sendAddProductRequestDetailed(storeName, product));
    }

    @Override
    public AppResult<String> updateProduct(String storeName, String productName, double newPrice, int newAmount) {
        AppResult<MasterCommunicator> communicatorResult = requireCommunicator();
        if (!communicatorResult.isSuccess()) {
            return AppResult.error(communicatorResult.getMessage());
        }
        return AppResult.success(
                communicatorResult.getData().sendUpdateProductRequestDetailed(storeName, productName, newPrice, newAmount)
        );
    }

    @Override
    public AppResult<String> removeProduct(String storeName, String productName) {
        AppResult<MasterCommunicator> communicatorResult = requireCommunicator();
        if (!communicatorResult.isSuccess()) {
            return AppResult.error(communicatorResult.getMessage());
        }
        return AppResult.success(communicatorResult.getData().sendRemoveProductRequestDetailed(storeName, productName));
    }
}
package com.example.restaurantapp;

import java.util.HashMap;
import java.util.Map;

public class FakeServerGateway implements ServerGateway {
    private AppResult<String> searchResult = AppResult.success("[]");
    private AppResult<String> partnerLoginResult = AppResult.success("OK");
    private AppResult<String> accessCodeResult = AppResult.success("CODE_SENT:test@example.com:123456:5");
    private AppResult<String> buyResult = AppResult.success("SUCCESS");
    private AppResult<String> addProductResult = AppResult.success("OK");
    private AppResult<String> updateProductResult = AppResult.success("OK");
    private AppResult<String> removeProductResult = AppResult.success("OK");
    private final Map<String, AppResult<String>> buyResultsByProductName = new HashMap<>();

    public FakeServerGateway withSearchResponse(String response) {
        this.searchResult = AppResult.success(response);
        return this;
    }

    public FakeServerGateway withSearchResult(AppResult<String> result) {
        this.searchResult = result;
        return this;
    }

    public FakeServerGateway withPartnerLoginResponse(String response) {
        this.partnerLoginResult = AppResult.success(response);
        return this;
    }

    public FakeServerGateway withAccessCodeResponse(String response) {
        this.accessCodeResult = AppResult.success(response);
        return this;
    }

    public FakeServerGateway withAddProductResponse(String response) {
        this.addProductResult = AppResult.success(response);
        return this;
    }

    public FakeServerGateway withUpdateProductResponse(String response) {
        this.updateProductResult = AppResult.success(response);
        return this;
    }

    public FakeServerGateway withRemoveProductResult(AppResult<String> result) {
        this.removeProductResult = result;
        return this;
    }

    public FakeServerGateway withBuyResponse(String response) {
        this.buyResult = AppResult.success(response);
        return this;
    }

    public FakeServerGateway withBuyResult(AppResult<String> result) {
        this.buyResult = result;
        return this;
    }

    public FakeServerGateway withBuyResponseForProduct(String productName, String response) {
        this.buyResultsByProductName.put(productName, AppResult.success(response));
        return this;
    }

    public FakeServerGateway withBuyResultForProduct(String productName, AppResult<String> result) {
        this.buyResultsByProductName.put(productName, result);
        return this;
    }

    @Override
    public AppResult<String> search(String latitude, String longitude, String foodCategory, String stars, String priceRange) {
        return searchResult;
    }

    @Override
    public AppResult<String> partnerLogin(String storeName, String accessCode) {
        return partnerLoginResult;
    }

    @Override
    public AppResult<String> requestPartnerAccessCode(String storeName) {
        return accessCodeResult;
    }

    @Override
    public AppResult<String> buy(String storeName, String productName, int quantity) {
        if (buyResultsByProductName.containsKey(productName)) {
            return buyResultsByProductName.get(productName);
        }
        return buyResult;
    }

    @Override
    public AppResult<String> addProduct(String storeName, Product product) {
        return addProductResult;
    }

    @Override
    public AppResult<String> updateProduct(String storeName, String productName, double newPrice, int newAmount) {
        return updateProductResult;
    }

    @Override
    public AppResult<String> removeProduct(String storeName, String productName) {
        return removeProductResult;
    }
}
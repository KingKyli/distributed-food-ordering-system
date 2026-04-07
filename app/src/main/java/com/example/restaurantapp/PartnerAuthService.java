package com.example.restaurantapp;

import java.util.List;

public class PartnerAuthService {
    private final RestaurantRepository restaurantRepository;
    private final ServerGateway serverGateway;

    public PartnerAuthService() {
        this(new RestaurantRepository(), new TcpServerGateway());
    }

    PartnerAuthService(RestaurantRepository restaurantRepository, ServerGateway serverGateway) {
        this.restaurantRepository = restaurantRepository;
        this.serverGateway = serverGateway;
    }

    public AppResult<List<Store>> loadStores() {
        return restaurantRepository.searchStores("", "", "", 0, "");
    }

    public AppResult<Store> loginPartner(String storeName, String password) {
        String normalizedStoreName = storeName == null ? "" : storeName.trim();
        String normalizedPassword = password == null ? "" : password.trim();

        if (normalizedStoreName.isEmpty() || normalizedPassword.isEmpty()) {
            return AppResult.error("Please fill in all fields.");
        }

        AppResult<String> responseResult = serverGateway.partnerLogin(normalizedStoreName, normalizedPassword);
        if (!responseResult.isSuccess()) {
            return AppResult.error(responseResult.getMessage());
        }
        String response = responseResult.getData();
        if (response == null || response.trim().isEmpty()) {
            return AppResult.error("Could not complete login. Please try again.");
        }
        if (!ProtocolUtils.isOkResponse(response)) {
            return AppResult.error(ProtocolUtils.extractErrorMessage(response, "Login failed."));
        }

        return restaurantRepository.fetchStoreByName(normalizedStoreName);
    }

    public AppResult<PartnerAccessCodeInfo> requestAccessCode(String storeName) {
        if (storeName == null || storeName.trim().isEmpty()) {
            return AppResult.error("Select your store first.");
        }

        AppResult<String> responseResult = serverGateway.requestPartnerAccessCode(storeName);
        if (!responseResult.isSuccess()) {
            return AppResult.error(responseResult.getMessage());
        }
        String response = responseResult.getData();
        if (response == null || response.trim().isEmpty()) {
            return AppResult.error("Could not request an access code.");
        }
        if (!response.startsWith("CODE_SENT:")) {
            return AppResult.error(ProtocolUtils.extractErrorMessage(response, "Could not request an access code."));
        }

        String[] parts = response.split(":", 4);
        if (parts.length < 4) {
            return AppResult.error("Invalid access code response from server.");
        }

        int expiresInMinutes;
        try {
            expiresInMinutes = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            expiresInMinutes = 5;
        }

        return AppResult.success(new PartnerAccessCodeInfo(storeName, parts[1], parts[2], expiresInMinutes));
    }
}


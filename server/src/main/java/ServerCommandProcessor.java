import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ServerCommandProcessor {
    private final PartnerAccessManager partnerAccessManager;
    private final StoreService storeService;

    ServerCommandProcessor(List<StoreData> stores, PartnerAccessManager partnerAccessManager, Runnable persistCallback) {
        this.partnerAccessManager = partnerAccessManager;
        this.storeService = new StoreService(stores, persistCallback);
    }

    String processCommand(String command) {
        if (command == null || command.isEmpty()) {
            return "ERROR: Empty command";
        }

        String[] parts = command.split(":", 2);
        String cmd = parts[0].toUpperCase();

        switch (cmd) {
            case "CLIENT_HELLO":
                return "SERVER_HELLO";
            case "SEARCH":
                return storeService.buildStoresJson();
            case "PARTNER_LOGIN":
                return handlePartnerLogin(parts.length > 1 ? parts[1] : "");
            case "REQUEST_PARTNER_ACCESS_CODE":
                return handleRequestPartnerAccessCode(parts.length > 1 ? parts[1] : "");
            case "GET_CREDENTIALS":
                return handleGetCredentials(parts.length > 1 ? parts[1] : "");
            case "BUY":
                return handleBuy(parts.length > 1 ? parts[1] : "");
            case "ADD_STORE":
                return handleAddStore(parts.length > 1 ? parts[1] : "");
            case "REMOVE_STORE":
                return handleRemoveStore(parts.length > 1 ? parts[1] : "");
            case "ADD_PRODUCT":
                return handleAddProduct(parts.length > 1 ? parts[1] : "");
            case "REMOVE_PRODUCT":
                return handleRemoveProduct(parts.length > 1 ? parts[1] : "");
            case "UPDATE_PRODUCT":
                return handleUpdateProduct(parts.length > 1 ? parts[1] : "");
            default:
                return "ERROR: Unknown command: " + cmd;
        }
    }

    private String handlePartnerLogin(String data) {
        String[] credentials = data.split(":", 2);
        if (credentials.length < 2) {
            return "ERROR: Invalid login format. Use PARTNER_LOGIN:storeName:accessCode";
        }
        String storeName = credentials[0].trim();
        String accessCode = credentials[1].trim();

        PartnerAccessManager.LoginValidationResult validationResult = partnerAccessManager.validateLogin(storeName, accessCode);
        if (validationResult == PartnerAccessManager.LoginValidationResult.STORE_NOT_FOUND) {
            System.out.println("    [LOGIN] Failed - Store not found: " + storeName);
            return "ERROR: Store not found";
        }
        if (validationResult == PartnerAccessManager.LoginValidationResult.NO_ACTIVE_CODE) {
            System.out.println("    [LOGIN] Failed - No active access code for: " + storeName);
            return "ERROR: Request a new access code first";
        }
        if (validationResult == PartnerAccessManager.LoginValidationResult.EXPIRED_CODE) {
            System.out.println("    [LOGIN] Failed - Expired access code for: " + storeName);
            return "ERROR: Access code expired. Request a new one";
        }
        if (validationResult == PartnerAccessManager.LoginValidationResult.SUCCESS) {
            System.out.println("    [LOGIN] Success for store: " + storeName);
            return "OK: Login successful";
        }

        System.out.println("    [LOGIN] Failed - Invalid access code for: " + storeName);
        return "ERROR: Invalid access code";
    }

    private String handleRequestPartnerAccessCode(String storeName) {
        String trimmedStoreName = storeName.trim();
        PartnerAccessManager.AccessCodeIssueResult result = partnerAccessManager.requestAccessCode(trimmedStoreName);
        if (!result.success) {
            return "ERROR: Store not found";
        }

        System.out.println("    [ACCESS_CODE] Generated code for " + trimmedStoreName + " sent to " + result.maskedDestination + ": " + result.accessCode);
        return "CODE_SENT:" + result.maskedDestination + ":" + result.accessCode + ":" + result.expiresInMinutes;
    }

    private String handleGetCredentials(String storeName) {
        String trimmedStoreName = storeName.trim();
        String maskedDestination = partnerAccessManager.getMaskedDestination(trimmedStoreName);
        if (maskedDestination != null) {
            System.out.println("    [CREDENTIALS] Returning delivery destination for: " + trimmedStoreName);
            return "DESTINATION:" + maskedDestination;
        }
        return "ERROR: Store not found";
    }

    private String handleBuy(String data) {
        String[] parts = data.split(":", 3);
        if (parts.length < 3) {
            return "ERROR: Invalid BUY format";
        }

        String storeName = parts[0].trim();
        String productName = parts[1].trim();
        int quantity;
        try {
            quantity = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException e) {
            return "ERROR: Invalid quantity";
        }

        StoreService.MutationResult result = storeService.buyProduct(storeName, productName, quantity);
        switch (result) {
            case STORE_NOT_FOUND:
                return "ERROR: Store not found";
            case PRODUCT_NOT_FOUND:
                return "ERROR: Product not found";
            case INVALID_QUANTITY:
                return "ERROR: Invalid quantity";
            case INSUFFICIENT_INVENTORY:
                return "ERROR: Not enough inventory";
            case SUCCESS:
                System.out.println("    [ORDER] Processed purchase: " + data);
                return "SUCCESS: Order placed successfully";
            default:
                return "ERROR: Unable to process order";
        }
    }

    private String handleAddStore(String json) {
        String storeName = extractJsonString(json, "StoreName");
        if (storeName == null || storeName.isEmpty()) {
            return "ERROR: Invalid store payload";
        }

        StoreData store = new StoreData(
                storeName,
                extractJsonDouble(json, "Latitude", 0.0),
                extractJsonDouble(json, "Longitude", 0.0),
                defaultString(extractJsonString(json, "FoodCategory"), "Unknown"),
                (int) extractJsonDouble(json, "Stars", 0.0),
                (int) extractJsonDouble(json, "NoOfVotes", 0.0),
                defaultString(extractJsonString(json, "StoreLogo"), "")
        );
        if (storeService.addStore(store) == StoreService.MutationResult.STORE_ALREADY_EXISTS) {
            return "ERROR: Store already exists";
        }
        return "OK: Store added";
    }

    private String handleRemoveStore(String storeName) {
        if (storeService.removeStore(storeName.trim()) == StoreService.MutationResult.SUCCESS) {
            return "OK: Store removed";
        }
        return "ERROR: Store not found";
    }

    private String handleAddProduct(String data) {
        int separator = data.indexOf(':');
        if (separator <= 0 || separator >= data.length() - 1) {
            return "ERROR: Invalid ADD_PRODUCT format";
        }

        String storeName = data.substring(0, separator).trim();
        String json = data.substring(separator + 1).trim();
        ProductData product = parseProductJson(json);
        if (product == null) {
            return "ERROR: Invalid product payload";
        }

        StoreService.MutationResult result = storeService.addProduct(storeName, product);
        if (result == StoreService.MutationResult.STORE_NOT_FOUND) {
            return "ERROR: Store not found";
        }
        if (result == StoreService.MutationResult.PRODUCT_ALREADY_EXISTS) {
            return "ERROR: Product already exists";
        }
        return "OK: Product added";
    }

    private String handleRemoveProduct(String data) {
        String[] parts = data.split(":", 2);
        if (parts.length < 2) {
            return "ERROR: Invalid REMOVE_PRODUCT format";
        }

        String storeName = parts[0].trim();
        String productName = parts[1].trim();

        StoreService.MutationResult result = storeService.removeProduct(storeName, productName);
        if (result == StoreService.MutationResult.STORE_NOT_FOUND) {
            return "ERROR: Store not found";
        }
        if (result == StoreService.MutationResult.SUCCESS) {
            return "OK: Product removed";
        }
        return "ERROR: Product not found";
    }

    private String handleUpdateProduct(String data) {
        String[] parts = data.split(":", 4);
        if (parts.length < 4) {
            return "ERROR: Invalid UPDATE_PRODUCT format";
        }

        String storeName = parts[0].trim();
        String productName = parts[1].trim();
        double newPrice;
        int newAmount;
        try {
            newPrice = Double.parseDouble(parts[2].trim());
            newAmount = Integer.parseInt(parts[3].trim());
        } catch (NumberFormatException e) {
            return "ERROR: Invalid update values";
        }

        StoreService.MutationResult result = storeService.updateProduct(storeName, productName, newPrice, newAmount);
        if (result == StoreService.MutationResult.STORE_NOT_FOUND) {
            return "ERROR: Store not found";
        }
        if (result == StoreService.MutationResult.SUCCESS) {
            return "OK: Product updated";
        }
        return "ERROR: Product not found";
    }

    private ProductData parseProductJson(String json) {
        String name = extractJsonString(json, "ProductName");
        String type = extractJsonString(json, "ProductType");
        Integer amount = extractJsonInt(json, "AvailableAmount");
        Double price = extractJsonDoubleObject(json, "Price");
        if (name == null || type == null || amount == null || price == null) {
            return null;
        }
        return new ProductData(name, type, amount, price);
    }

    private String extractJsonString(String json, String key) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"(.*?)\\\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? JsonSupport.unescapeJson(matcher.group(1)) : null;
    }

    private Integer extractJsonInt(String json, String key) {
        Double value = extractJsonDoubleObject(json, key);
        return value == null ? null : value.intValue();
    }

    private double extractJsonDouble(String json, String key, double defaultValue) {
        Double value = extractJsonDoubleObject(json, key);
        return value == null ? defaultValue : value;
    }

    private Double extractJsonDoubleObject(String json, String key) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String defaultString(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }
}
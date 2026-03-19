import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple Mock Server for testing the Restaurant App
 * Run with: java MockServer.java
 * Or compile first: javac MockServer.java && java MockServer
 */
public class MockServer {
    private static final int PORT = 8765;
    
    // Partner registry - StoreName -> Registered delivery destination
    private static final Map<String, String> PARTNER_CONTACTS = new HashMap<>() {{
        put("Pizza Palace", "ops@pizzapalace.foodie-demo.com");
        put("Burger Barn", "ops@burgerbarn.foodie-demo.com");
        put("Sushi Sun", "ops@sushisun.foodie-demo.com");
        put("Taco Town", "ops@tacotown.foodie-demo.com");
        put("Pasta Paradise", "ops@pastaparadise.foodie-demo.com");
        put("Gyros Express", "ops@gyrosexpress.foodie-demo.com");
    }};
    private static final Map<String, String> ACTIVE_PARTNER_CODES = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, Long> ACTIVE_PARTNER_CODE_EXPIRY = Collections.synchronizedMap(new HashMap<>());
    private static final int ACCESS_CODE_EXPIRY_MINUTES = 5;
    
    private static final List<StoreData> STORES = Collections.synchronizedList(createSeedStores());

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  Mock Server for Restaurant App");
        System.out.println("===========================================");
        System.out.println("Starting server on port " + PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Print all available IPs
            System.out.println("\nServer is running! Connect using one of these addresses:");
            System.out.println("  - From Android Emulator: 10.0.2.2:" + PORT);
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iface = interfaces.nextElement();
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                            System.out.println("  - From physical device: " + addr.getHostAddress() + ":" + PORT);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
            System.out.println("\nWaiting for connections...\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[+] Client connected: " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("  <- Received: " + line);
                String response = processCommand(line);
                System.out.println("  -> Sending: " + (response.length() > 100 ? response.substring(0, 100) + "..." : response));
                out.println(response);
            }
        } catch (IOException e) {
            System.out.println("[-] Client disconnected: " + e.getMessage());
        }
    }

    private static String processCommand(String command) {
        if (command == null || command.isEmpty()) {
            return "ERROR: Empty command";
        }

        String[] parts = command.split(":", 2);
        String cmd = parts[0].toUpperCase();

        switch (cmd) {
            case "CLIENT_HELLO":
                return "SERVER_HELLO";
                
            case "SEARCH":
                return buildStoresJson();
            
            case "PARTNER_LOGIN":
                // PARTNER_LOGIN:storeName:password
                return handlePartnerLogin(parts.length > 1 ? parts[1] : "");

            case "REQUEST_PARTNER_ACCESS_CODE":
                return handleRequestPartnerAccessCode(parts.length > 1 ? parts[1] : "");
            
            case "GET_CREDENTIALS":
                // GET_CREDENTIALS:storeName - Returns the password for demo purposes
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
    
    private static String handlePartnerLogin(String data) {
        String[] credentials = data.split(":", 2);
        if (credentials.length < 2) {
            return "ERROR: Invalid login format. Use PARTNER_LOGIN:storeName:accessCode";
        }
        String storeName = credentials[0].trim();
        String accessCode = credentials[1].trim();
        
        String registeredDestination = PARTNER_CONTACTS.get(storeName);
        if (registeredDestination == null) {
            System.out.println("    [LOGIN] Failed - Store not found: " + storeName);
            return "ERROR: Store not found";
        }

        String expectedCode = ACTIVE_PARTNER_CODES.get(storeName);
        Long expiresAt = ACTIVE_PARTNER_CODE_EXPIRY.get(storeName);
        if (expectedCode == null || expiresAt == null) {
            System.out.println("    [LOGIN] Failed - No active access code for: " + storeName);
            return "ERROR: Request a new access code first";
        }
        if (System.currentTimeMillis() > expiresAt) {
            ACTIVE_PARTNER_CODES.remove(storeName);
            ACTIVE_PARTNER_CODE_EXPIRY.remove(storeName);
            System.out.println("    [LOGIN] Failed - Expired access code for: " + storeName);
            return "ERROR: Access code expired. Request a new one";
        }
        
        if (expectedCode.equals(accessCode)) {
            ACTIVE_PARTNER_CODES.remove(storeName);
            ACTIVE_PARTNER_CODE_EXPIRY.remove(storeName);
            System.out.println("    [LOGIN] Success for store: " + storeName);
            return "OK: Login successful";
        } else {
            System.out.println("    [LOGIN] Failed - Invalid access code for: " + storeName);
            return "ERROR: Invalid access code";
        }
    }

    private static String handleRequestPartnerAccessCode(String storeName) {
        storeName = storeName.trim();
        String deliveryDestination = PARTNER_CONTACTS.get(storeName);
        if (deliveryDestination == null) {
            return "ERROR: Store not found";
        }

        String accessCode = generateSixDigitCode();
        ACTIVE_PARTNER_CODES.put(storeName, accessCode);
        ACTIVE_PARTNER_CODE_EXPIRY.put(storeName, System.currentTimeMillis() + ACCESS_CODE_EXPIRY_MINUTES * 60_000L);

        String maskedDestination = maskEmail(deliveryDestination);
        System.out.println("    [ACCESS_CODE] Generated code for " + storeName + " sent to " + maskedDestination + ": " + accessCode);
        return "CODE_SENT:" + maskedDestination + ":" + accessCode + ":" + ACCESS_CODE_EXPIRY_MINUTES;
    }
    
    private static String handleGetCredentials(String storeName) {
        storeName = storeName.trim();
        String contact = PARTNER_CONTACTS.get(storeName);
        if (contact != null) {
            System.out.println("    [CREDENTIALS] Returning delivery destination for: " + storeName);
            return "DESTINATION:" + maskEmail(contact);
        }
        return "ERROR: Store not found";
    }

    private static String generateSixDigitCode() {
        int value = 100000 + new Random().nextInt(900000);
        return String.valueOf(value);
    }

    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.substring(0, 2) + "***" + domain;
    }

    private static String handleBuy(String data) {
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

        synchronized (STORES) {
            StoreData store = findStore(storeName);
            if (store == null) {
                return "ERROR: Store not found";
            }
            ProductData product = store.findProduct(productName);
            if (product == null) {
                return "ERROR: Product not found";
            }
            if (quantity <= 0 || product.availableAmount < quantity) {
                return "ERROR: Not enough inventory";
            }
            product.availableAmount -= quantity;
        }

        System.out.println("    [ORDER] Processed purchase: " + data);
        return "SUCCESS: Order placed successfully";
    }

    private static String handleAddStore(String json) {
        String storeName = extractJsonString(json, "StoreName");
        if (storeName == null || storeName.isEmpty()) {
            return "ERROR: Invalid store payload";
        }

        synchronized (STORES) {
            if (findStore(storeName) != null) {
                return "ERROR: Store already exists";
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
            STORES.add(store);
        }
        return "OK: Store added";
    }

    private static String handleRemoveStore(String storeName) {
        storeName = storeName.trim();
        synchronized (STORES) {
            Iterator<StoreData> iterator = STORES.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().storeName.equalsIgnoreCase(storeName)) {
                    iterator.remove();
                    return "OK: Store removed";
                }
            }
        }
        return "ERROR: Store not found";
    }

    private static String handleAddProduct(String data) {
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

        synchronized (STORES) {
            StoreData store = findStore(storeName);
            if (store == null) {
                return "ERROR: Store not found";
            }
            if (store.findProduct(product.productName) != null) {
                return "ERROR: Product already exists";
            }
            store.products.add(product);
        }

        return "OK: Product added";
    }

    private static String handleRemoveProduct(String data) {
        String[] parts = data.split(":", 2);
        if (parts.length < 2) {
            return "ERROR: Invalid REMOVE_PRODUCT format";
        }

        String storeName = parts[0].trim();
        String productName = parts[1].trim();

        synchronized (STORES) {
            StoreData store = findStore(storeName);
            if (store == null) {
                return "ERROR: Store not found";
            }
            Iterator<ProductData> iterator = store.products.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().productName.equalsIgnoreCase(productName)) {
                    iterator.remove();
                    return "OK: Product removed";
                }
            }
        }

        return "ERROR: Product not found";
    }

    private static String handleUpdateProduct(String data) {
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

        synchronized (STORES) {
            StoreData store = findStore(storeName);
            if (store == null) {
                return "ERROR: Store not found";
            }
            ProductData product = store.findProduct(productName);
            if (product == null) {
                return "ERROR: Product not found";
            }
            product.price = newPrice;
            product.availableAmount = newAmount;
        }

        return "OK: Product updated";
    }

    private static List<StoreData> createSeedStores() {
        List<StoreData> stores = new ArrayList<>();

        stores.add(new StoreData("Pizza Palace", 37.9838, 23.7275, "Pizza", 4, 120, "pizza_logo",
                new ProductData("Margherita", "Pizza", 50, 8.99),
                new ProductData("Pepperoni", "Pizza", 40, 10.99),
                new ProductData("Hawaiian", "Pizza", 30, 11.99)));

        stores.add(new StoreData("Burger Barn", 37.9750, 23.7350, "Burgers", 4, 85, "burger_logo",
                new ProductData("Classic Burger", "Burger", 60, 7.99),
                new ProductData("Cheese Burger", "Burger", 45, 8.99),
                new ProductData("Bacon Burger", "Burger", 35, 9.99)));

        stores.add(new StoreData("Sushi Sun", 37.9900, 23.7200, "Sushi", 5, 200, "sushi_logo",
                new ProductData("Salmon Roll", "Sushi", 25, 12.99),
                new ProductData("Tuna Sashimi", "Sushi", 20, 14.99),
                new ProductData("Dragon Roll", "Sushi", 15, 16.99)));

        stores.add(new StoreData("Taco Town", 37.9780, 23.7400, "Mexican", 4, 65, "taco_logo",
                new ProductData("Beef Taco", "Taco", 100, 3.99),
                new ProductData("Chicken Burrito", "Burrito", 80, 6.99),
                new ProductData("Nachos", "Appetizer", 50, 5.99)));

        stores.add(new StoreData("Pasta Paradise", 37.9820, 23.7150, "Italian", 5, 150, "pasta_logo",
                new ProductData("Spaghetti Carbonara", "Pasta", 40, 11.99),
                new ProductData("Lasagna", "Pasta", 30, 13.99),
                new ProductData("Fettuccine Alfredo", "Pasta", 35, 12.99)));

        stores.add(new StoreData("Gyros Express", 37.9860, 23.7300, "Greek", 4, 95, "gyros_logo",
                new ProductData("Pork Gyros", "Gyros", 70, 4.99),
                new ProductData("Chicken Souvlaki", "Souvlaki", 60, 5.99),
                new ProductData("Greek Salad", "Salad", 40, 6.99)));

        return stores;
    }

    private static StoreData findStore(String storeName) {
        for (StoreData store : STORES) {
            if (store.storeName.equalsIgnoreCase(storeName)) {
                return store;
            }
        }
        return null;
    }

    private static ProductData parseProductJson(String json) {
        String name = extractJsonString(json, "ProductName");
        String type = extractJsonString(json, "ProductType");
        Integer amount = extractJsonInt(json, "AvailableAmount");
        Double price = extractJsonDoubleObject(json, "Price");
        if (name == null || type == null || amount == null || price == null) {
            return null;
        }
        return new ProductData(name, type, amount, price);
    }

    private static String buildStoresJson() {
        StringBuilder sb = new StringBuilder("[");
        synchronized (STORES) {
            for (int i = 0; i < STORES.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(STORES.get(i).toJson());
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private static String extractJsonString(String json, String key) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"(.*?)\\\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? unescapeJson(matcher.group(1)) : null;
    }

    private static Integer extractJsonInt(String json, String key) {
        Double value = extractJsonDoubleObject(json, key);
        return value == null ? null : value.intValue();
    }

    private static double extractJsonDouble(String json, String key, double defaultValue) {
        Double value = extractJsonDoubleObject(json, key);
        return value == null ? defaultValue : value;
    }

    private static Double extractJsonDoubleObject(String json, String key) {
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

    private static String unescapeJson(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String defaultString(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static final class StoreData {
        private final String storeName;
        private final double latitude;
        private final double longitude;
        private final String foodCategory;
        private final int stars;
        private final int noOfVotes;
        private final String storeLogo;
        private final List<ProductData> products = new ArrayList<>();

        private StoreData(String storeName, double latitude, double longitude, String foodCategory, int stars, int noOfVotes, String storeLogo, ProductData... products) {
            this(storeName, latitude, longitude, foodCategory, stars, noOfVotes, storeLogo);
            this.products.addAll(Arrays.asList(products));
        }

        private StoreData(String storeName, double latitude, double longitude, String foodCategory, int stars, int noOfVotes, String storeLogo) {
            this.storeName = storeName;
            this.latitude = latitude;
            this.longitude = longitude;
            this.foodCategory = foodCategory;
            this.stars = stars;
            this.noOfVotes = noOfVotes;
            this.storeLogo = storeLogo;
        }

        private ProductData findProduct(String productName) {
            for (ProductData product : products) {
                if (product.productName.equalsIgnoreCase(productName)) {
                    return product;
                }
            }
            return null;
        }

        private String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append('{')
                    .append("\"StoreName\":\"").append(escapeJson(storeName)).append("\",")
                    .append("\"Latitude\":").append(latitude).append(',')
                    .append("\"Longitude\":").append(longitude).append(',')
                    .append("\"FoodCategory\":\"").append(escapeJson(foodCategory)).append("\",")
                    .append("\"Stars\":").append(stars).append(',')
                    .append("\"NoOfVotes\":").append(noOfVotes).append(',')
                    .append("\"StoreLogo\":\"").append(escapeJson(storeLogo)).append("\",")
                    .append("\"Products\":[");
            for (int i = 0; i < products.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(products.get(i).toJson());
            }
            sb.append("]}");
            return sb.toString();
        }
    }

    private static final class ProductData {
        private final String productName;
        private final String productType;
        private int availableAmount;
        private double price;

        private ProductData(String productName, String productType, int availableAmount, double price) {
            this.productName = productName;
            this.productType = productType;
            this.availableAmount = availableAmount;
            this.price = price;
        }

        private String toJson() {
            return "{" +
                    "\"ProductName\":\"" + escapeJson(productName) + "\"," +
                    "\"ProductType\":\"" + escapeJson(productType) + "\"," +
                    "\"AvailableAmount\":" + availableAmount + "," +
                    "\"Price\":" + price +
                    "}";
        }
    }
}

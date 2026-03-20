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
        put("Pizza Palace",         "ops@pizzapalace.foodie-demo.com");
        put("Burger Barn",          "ops@burgerbarn.foodie-demo.com");
        put("Sushi Sun",            "ops@sushisun.foodie-demo.com");
        put("Taco Town",            "ops@tacotown.foodie-demo.com");
        put("Pasta Paradise",       "ops@pastaparadise.foodie-demo.com");
        put("Gyros Express",        "ops@gyrosexpress.foodie-demo.com");
        put("Souvlaki Square",      "ops@souvlakisquare.foodie-demo.com");
        put("Kebab House",          "ops@kebabhouse.foodie-demo.com");
        put("Noodle Nook",          "ops@noodlenook.foodie-demo.com");
        put("Indian Spice Garden",  "ops@indianspice.foodie-demo.com");
        put("Le Bistro Paris",      "ops@lebistro.foodie-demo.com");
        put("Steakhouse Seven",     "ops@steakhouseseven.foodie-demo.com");
        put("The Vegan Garden",     "ops@vegangarden.foodie-demo.com");
        put("BBQ Nation",           "ops@bbqnation.foodie-demo.com");
        put("Cookie Corner",        "ops@cookiecorner.foodie-demo.com");
        put("Falafel House",        "ops@falafelhouse.foodie-demo.com");
        put("Ramen Republic",       "ops@ramenrepublic.foodie-demo.com");
        put("Tex Mex Grill",        "ops@texmexgrill.foodie-demo.com");
        put("The Sandwich Bar",     "ops@sandwichbar.foodie-demo.com");
        put("Greek Tavern",         "ops@greektavern.foodie-demo.com");
        put("Thai Palace",          "ops@thaipalace.foodie-demo.com");
        put("Crepe Cafe",           "ops@crepecafe.foodie-demo.com");
        put("Dim Sum Dragon",       "ops@dimsumdragon.foodie-demo.com");
        put("Poke Bowl Bar",        "ops@pokebowlbar.foodie-demo.com");
        put("Brunch Club",          "ops@brunchclub.foodie-demo.com");
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

        // ── ★★★★★ TOP-RATED ─────────────────────────────────────────────
        stores.add(new StoreData("Sushi Sun", 37.9900, 23.7200, "Sushi", 5, 312, "sushi_logo",
                new ProductData("Salmon Roll",    "Sushi",  25, 12.99),
                new ProductData("Tuna Sashimi",   "Sushi",  20, 14.99),
                new ProductData("Dragon Roll",    "Sushi",  15, 16.99),
                new ProductData("Miso Soup",      "Soup",   40,  3.99)));

        stores.add(new StoreData("Pasta Paradise", 37.9820, 23.7150, "Italian", 5, 210, "pasta_logo",
                new ProductData("Spaghetti Carbonara", "Pasta",  40, 11.99),
                new ProductData("Lasagna",              "Pasta",  30, 13.99),
                new ProductData("Fettuccine Alfredo",   "Pasta",  35, 12.99),
                new ProductData("Tiramisu",             "Dessert",50,  5.99)));

        stores.add(new StoreData("Ramen Republic", 37.9910, 23.7350, "Japanese", 5, 178, "ramen_logo",
                new ProductData("Tonkotsu Ramen",  "Ramen",  30, 13.50),
                new ProductData("Shoyu Ramen",     "Ramen",  28, 12.50),
                new ProductData("Gyoza (6 pcs)",   "Starter",40,  6.99),
                new ProductData("Edamame",         "Starter",60,  3.50)));

        stores.add(new StoreData("Le Bistro Paris", 37.9760, 23.7180, "French", 5, 145, "bistro_logo",
                new ProductData("Croque Monsieur", "Sandwich", 20, 14.99),
                new ProductData("French Onion Soup","Soup",    15, 11.99),
                new ProductData("Beef Bourguignon","Main",     12, 22.99),
                new ProductData("Creme Brulee",    "Dessert",  25,  9.99)));

        stores.add(new StoreData("Greek Tavern", 37.9830, 23.7050, "Greek", 5, 289, "tavern_logo",
                new ProductData("Moussaka",         "Main",   35, 14.99),
                new ProductData("Lamb Chops",        "Main",   20, 19.99),
                new ProductData("Spanakopita",       "Starter",50,  7.99),
                new ProductData("Baklava",           "Dessert",80,  4.99)));

        stores.add(new StoreData("Indian Spice Garden", 37.9870, 23.7420, "Indian", 5, 196, "indian_logo",
                new ProductData("Chicken Tikka Masala","Curry", 30, 13.99),
                new ProductData("Lamb Biryani",        "Rice",  25, 14.99),
                new ProductData("Garlic Naan",         "Bread", 80,  2.99),
                new ProductData("Mango Lassi",         "Drink", 60,  3.99)));

        stores.add(new StoreData("The Poke Bowl Bar", 37.9770, 23.7310, "Hawaiian", 5, 167, "poke_logo",
                new ProductData("Ahi Tuna Bowl",    "Bowl",   25, 14.99),
                new ProductData("Salmon Poke Bowl", "Bowl",   30, 13.99),
                new ProductData("Veggie Poke Bowl", "Bowl",   35, 11.99),
                new ProductData("Edamame Side",     "Side",   50,  3.99)));

        // ── ★★★★ GREAT ──────────────────────────────────────────────────
        stores.add(new StoreData("Pizza Palace", 37.9838, 23.7275, "Pizza", 4, 120, "pizza_logo",
                new ProductData("Margherita",      "Pizza",  50,  8.99),
                new ProductData("Pepperoni",        "Pizza",  40, 10.99),
                new ProductData("Hawaiian",         "Pizza",  30, 11.99),
                new ProductData("Garlic Bread",     "Side",   70,  3.99)));

        stores.add(new StoreData("Burger Barn", 37.9750, 23.7350, "Burgers", 4, 85, "burger_logo",
                new ProductData("Classic Burger",  "Burger", 60,  7.99),
                new ProductData("Cheese Burger",   "Burger", 45,  8.99),
                new ProductData("Bacon Burger",    "Burger", 35,  9.99),
                new ProductData("Fries",           "Side",   90,  2.99)));

        stores.add(new StoreData("Gyros Express", 37.9860, 23.7300, "Greek", 4, 95, "gyros_logo",
                new ProductData("Pork Gyros",      "Gyros",    70,  4.99),
                new ProductData("Chicken Souvlaki","Souvlaki", 60,  5.99),
                new ProductData("Greek Salad",     "Salad",    40,  6.99),
                new ProductData("Tzatziki",        "Side",    100,  2.50)));

        stores.add(new StoreData("Souvlaki Square", 37.9895, 23.7260, "Greek Street Food", 4, 143, "souvlaki_logo",
                new ProductData("Pita Wrap",       "Wrap",    90,  3.99),
                new ProductData("Kalamaki Pork",   "Skewer",  70,  4.50),
                new ProductData("Kalamaki Chicken","Skewer",  65,  4.50),
                new ProductData("Chips",           "Side",   120,  2.00)));

        stores.add(new StoreData("Noodle Nook", 37.9805, 23.7220, "Asian Fusion", 4, 112, "noodle_logo",
                new ProductData("Pad Thai",        "Noodles", 40, 10.99),
                new ProductData("Spring Rolls",    "Starter", 60,  5.99),
                new ProductData("Fried Rice",      "Rice",    50,  9.99),
                new ProductData("Wonton Soup",     "Soup",    35,  6.99)));

        stores.add(new StoreData("BBQ Nation", 37.9745, 23.7390, "BBQ", 4, 98, "bbq_logo",
                new ProductData("BBQ Ribs Half",   "BBQ",    20, 16.99),
                new ProductData("Pulled Pork Sandwich","Sandwich",40, 9.99),
                new ProductData("Corn on the Cob", "Side",   60,  3.50),
                new ProductData("Coleslaw",        "Side",   80,  2.50)));

        stores.add(new StoreData("Thai Palace", 37.9850, 23.7470, "Thai", 4, 134, "thai_logo",
                new ProductData("Green Curry",     "Curry",  35, 12.99),
                new ProductData("Tom Yum Soup",    "Soup",   30,  9.99),
                new ProductData("Mango Sticky Rice","Dessert",40,  5.99),
                new ProductData("Jasmine Rice",    "Rice",   90,  2.50)));

        stores.add(new StoreData("Dim Sum Dragon", 37.9920, 23.7155, "Chinese", 4, 156, "dimsum_logo",
                new ProductData("Har Gow (4 pcs)", "Dim Sum",35,  7.99),
                new ProductData("Siu Mai (4 pcs)", "Dim Sum",40,  7.50),
                new ProductData("Char Siu Bao",   "Bun",    50,  5.99),
                new ProductData("Egg Tart",        "Dessert",70,  2.99)));

        stores.add(new StoreData("The Vegan Garden", 37.9780, 23.7195, "Vegan", 4, 88, "vegan_logo",
                new ProductData("Falafel Wrap",    "Wrap",   45,  8.99),
                new ProductData("Quinoa Bowl",     "Bowl",   35, 11.99),
                new ProductData("Avocado Toast",   "Toast",  40,  7.99),
                new ProductData("Fruit Smoothie",  "Drink",  60,  4.99)));

        stores.add(new StoreData("Steakhouse Seven", 37.9710, 23.7280, "Steakhouse", 4, 76, "steak_logo",
                new ProductData("Ribeye 250g",     "Steak",  15, 26.99),
                new ProductData("Sirloin 200g",    "Steak",  18, 22.99),
                new ProductData("Caesar Salad",    "Salad",  30,  8.99),
                new ProductData("Mashed Potatoes", "Side",   60,  4.99)));

        stores.add(new StoreData("Brunch Club", 37.9930, 23.7340, "Brunch", 4, 201, "brunch_logo",
                new ProductData("Eggs Benedict",   "Breakfast",30, 11.99),
                new ProductData("Avocado Shakshuka","Breakfast",25, 12.99),
                new ProductData("Pancake Stack",   "Breakfast",50,  8.99),
                new ProductData("Fresh Orange Juice","Drink", 80,  4.50)));

        stores.add(new StoreData("Crepe Cafe", 37.9800, 23.7440, "Crepes", 4, 119, "crepe_logo",
                new ProductData("Nutella Crepe",   "Crepe",  60,  5.99),
                new ProductData("Ham & Cheese Crepe","Crepe",50,  6.99),
                new ProductData("Strawberry Crepe","Crepe",  55,  6.50),
                new ProductData("Hot Chocolate",   "Drink",  80,  3.50)));

        // ── ★★★ GOOD ─────────────────────────────────────────────────────
        stores.add(new StoreData("Taco Town", 37.9780, 23.7400, "Mexican", 3, 65, "taco_logo",
                new ProductData("Beef Taco",       "Taco",   100,  3.99),
                new ProductData("Chicken Burrito", "Burrito", 80,  6.99),
                new ProductData("Nachos",          "Appetizer",50, 5.99),
                new ProductData("Guacamole",       "Side",    60,  2.99)));

        stores.add(new StoreData("Kebab House", 37.9715, 23.7320, "Middle Eastern", 3, 74, "kebab_logo",
                new ProductData("Doner Kebab",     "Kebab",   80,  5.50),
                new ProductData("Shish Kebab",     "Kebab",   60,  7.99),
                new ProductData("Hummus & Pita",   "Starter", 70,  4.50),
                new ProductData("Baklava",         "Dessert", 50,  2.99)));

        stores.add(new StoreData("Falafel House", 37.9940, 23.7380, "Mediterranean", 3, 91, "falafel_logo",
                new ProductData("Falafel Plate",   "Main",    50,  6.99),
                new ProductData("Shawarma Wrap",   "Wrap",    60,  5.99),
                new ProductData("Hummus",          "Side",    80,  3.50),
                new ProductData("Lemonade",        "Drink",  100,  2.50)));

        stores.add(new StoreData("Tex Mex Grill", 37.9730, 23.7450, "Mexican", 3, 52, "texmex_logo",
                new ProductData("Quesadilla",      "Mexican", 45,  7.99),
                new ProductData("Beef Nachos",     "Mexican", 40,  8.99),
                new ProductData("Chicken Fajitas", "Mexican", 30, 11.99),
                new ProductData("Salsa & Chips",   "Starter", 60,  4.50)));

        stores.add(new StoreData("The Sandwich Bar", 37.9885, 23.7100, "Sandwiches", 3, 83, "sandwich_logo",
                new ProductData("Club Sandwich",   "Sandwich",50,  7.50),
                new ProductData("BLT",             "Sandwich",40,  5.99),
                new ProductData("Grilled Chicken", "Sandwich",35,  6.99),
                new ProductData("Iced Coffee",     "Drink",  100,  3.50)));

        stores.add(new StoreData("Cookie Corner", 37.9960, 23.7230, "Desserts", 3, 108, "cookie_logo",
                new ProductData("Choc Chip Cookie","Cookie",  200, 1.99),
                new ProductData("Brownie",         "Cake",    150, 2.99),
                new ProductData("Cheesecake Slice","Cake",     80, 4.99),
                new ProductData("Milkshake",       "Drink",   60,  5.99)));

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

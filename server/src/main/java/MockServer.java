import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mock Server for testing the Restaurant App.
 * Recommended run command: .\gradlew.bat :server:run
 */
public class MockServer {
    private static final int PORT = 8765;
    private static final Path DATA_DIRECTORY = Paths.get(System.getProperty("user.dir"), "server-data");
    private static final Path DATABASE_PATH = DATA_DIRECTORY.resolve("restaurant-app.db");
    private static final Logger LOGGER = ServerLog.getLogger(MockServer.class);

    private static final Map<String, String> PARTNER_CONTACTS = createPartnerContacts();
    private static final PartnerAccessManager PARTNER_ACCESS_MANAGER = new PartnerAccessManager(PARTNER_CONTACTS);
    private static final StoreRepository STORE_REPOSITORY = new StoreRepository(DATA_DIRECTORY, DATABASE_PATH);
    private static final List<StoreData> STORES = Collections.synchronizedList(new ArrayList<>());
    private static final ServerCommandProcessor COMMAND_PROCESSOR = new ServerCommandProcessor(
            STORES,
            PARTNER_ACCESS_MANAGER,
            MockServer::persistStoresToDatabase
    );

    public static void main(String[] args) {
        LOGGER.info("===========================================");
        LOGGER.info("  Mock Server for Restaurant App");
        LOGGER.info("===========================================");
        LOGGER.info("Starting server on port " + PORT + "...");
        bootstrapStoreState();
        Runtime.getRuntime().addShutdownHook(new Thread(MockServer::persistStoresToDatabase));
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Print all available IPs
            LOGGER.info("Server is running. Connect using one of these addresses:");
            LOGGER.info("From Android Emulator: 10.0.2.2:" + PORT);
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iface = interfaces.nextElement();
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                            LOGGER.info("From physical device: " + addr.getHostAddress() + ":" + PORT);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Unable to enumerate network interfaces", e);
            }
            LOGGER.info("SQLite database: " + DATABASE_PATH.toAbsolutePath());
            LOGGER.info("Waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                LOGGER.info("Client connected: " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server error", e);
        }
    }

    private static void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                LOGGER.fine("Received command: " + line);
                String response = COMMAND_PROCESSOR.processCommand(line);
                LOGGER.fine("Sending response: " + (response.length() > 100 ? response.substring(0, 100) + "..." : response));
                out.println(response);
            }
        } catch (IOException e) {
            LOGGER.fine("Client disconnected: " + e.getMessage());
        }
    }

    private static void bootstrapStoreState() {
        List<StoreData> loadedStores = STORE_REPOSITORY.bootstrap(StoreSeedData.createSeedStores());
        synchronized (STORES) {
            STORES.clear();
            STORES.addAll(loadedStores);
        }
        LOGGER.info("Loaded " + STORES.size() + " stores from SQLite.");
    }

    private static void persistStoresToDatabase() {
        synchronized (STORES) {
            STORE_REPOSITORY.saveStores(STORES);
        }
    }

    private static Map<String, String> createPartnerContacts() {
        Map<String, String> contacts = new LinkedHashMap<>();
        contacts.put("Pizza Palace", "ops@pizzapalace.foodie-demo.com");
        contacts.put("Burger Barn", "ops@burgerbarn.foodie-demo.com");
        contacts.put("Sushi Sun", "ops@sushisun.foodie-demo.com");
        contacts.put("Taco Town", "ops@tacotown.foodie-demo.com");
        contacts.put("Pasta Paradise", "ops@pastaparadise.foodie-demo.com");
        contacts.put("Gyros Express", "ops@gyrosexpress.foodie-demo.com");
        contacts.put("Souvlaki Square", "ops@souvlakisquare.foodie-demo.com");
        contacts.put("Kebab House", "ops@kebabhouse.foodie-demo.com");
        contacts.put("Noodle Nook", "ops@noodlenook.foodie-demo.com");
        contacts.put("Indian Spice Garden", "ops@indianspice.foodie-demo.com");
        contacts.put("Le Bistro Paris", "ops@lebistro.foodie-demo.com");
        contacts.put("Steakhouse Seven", "ops@steakhouseseven.foodie-demo.com");
        contacts.put("The Vegan Garden", "ops@vegangarden.foodie-demo.com");
        contacts.put("BBQ Nation", "ops@bbqnation.foodie-demo.com");
        contacts.put("Cookie Corner", "ops@cookiecorner.foodie-demo.com");
        contacts.put("Falafel House", "ops@falafelhouse.foodie-demo.com");
        contacts.put("Ramen Republic", "ops@ramenrepublic.foodie-demo.com");
        contacts.put("Tex Mex Grill", "ops@texmexgrill.foodie-demo.com");
        contacts.put("The Sandwich Bar", "ops@sandwichbar.foodie-demo.com");
        contacts.put("Greek Tavern", "ops@greektavern.foodie-demo.com");
        contacts.put("Thai Palace", "ops@thaipalace.foodie-demo.com");
        contacts.put("Crepe Cafe", "ops@crepecafe.foodie-demo.com");
        contacts.put("Dim Sum Dragon", "ops@dimsumdragon.foodie-demo.com");
        contacts.put("Poke Bowl Bar", "ops@pokebowlbar.foodie-demo.com");
        contacts.put("Brunch Club", "ops@brunchclub.foodie-demo.com");
        return contacts;
    }
}
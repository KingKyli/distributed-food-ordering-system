import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtocolIntegrationTest {

    @Test
    void socketFlowSupportsHelloSearchAndBuy() throws Exception {
        try (ProtocolTestServer server = new ProtocolTestServer()) {
            server.start();
            try (Socket client = new Socket("127.0.0.1", server.port());
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

                out.println("CLIENT_HELLO");
                assertEquals("SERVER_HELLO", in.readLine());

                out.println("SEARCH");
                String searchResponse = in.readLine();
                assertTrue(searchResponse.startsWith("["));
                assertTrue(searchResponse.contains("Burger Barn"));

                out.println("BUY:Burger Barn:Classic Burger:1");
                assertEquals("SUCCESS: Order placed successfully", in.readLine());

                out.println("BUY:Burger Barn:Classic Burger:100");
                assertEquals("ERROR: Not enough inventory", in.readLine());
            }
        }
    }

    @Test
    void socketFlowValidatesPartnerCodeLifecycle() throws Exception {
        try (ProtocolTestServer server = new ProtocolTestServer()) {
            server.start();
            try (Socket client = new Socket("127.0.0.1", server.port());
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

                out.println("REQUEST_PARTNER_ACCESS_CODE:Pizza Palace");
                String codeResponse = in.readLine();
                assertTrue(codeResponse.startsWith("CODE_SENT:"));

                String[] codeParts = codeResponse.split(":");
                String accessCode = codeParts[2];

                out.println("REQUEST_PARTNER_ACCESS_CODE:Pizza Palace");
                assertTrue(in.readLine().startsWith("ERROR: Access code already sent."));

                out.println("PARTNER_LOGIN:Pizza Palace:12ab");
                assertEquals("ERROR: Access code must be 6 digits", in.readLine());

                out.println("PARTNER_LOGIN:Pizza Palace:" + accessCode);
                assertEquals("OK: Login successful", in.readLine());
            }
        }
    }

    private static final class ProtocolTestServer implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final Thread serverThread;

        ProtocolTestServer() throws IOException {
            this.serverSocket = new ServerSocket(0);
            this.serverThread = new Thread(this::acceptAndServeSingleClient, "protocol-test-server");
            this.serverThread.setDaemon(true);
        }

        void start() {
            this.serverThread.start();
        }

        int port() {
            return serverSocket.getLocalPort();
        }

        private void acceptAndServeSingleClient() {
            List<StoreData> stores = new ArrayList<>();
            stores.add(new StoreData(
                    "Burger Barn",
                    1.0,
                    2.0,
                    "Burgers",
                    4,
                    10,
                    "logo",
                    new ProductData("Classic Burger", "Main", 5, 9.99)
            ));

            ServerCommandProcessor processor = new ServerCommandProcessor(
                    stores,
                    new PartnerAccessManager(Map.of("Pizza Palace", "ops@pizzapalace.foodie-demo.com")),
                    () -> { }
            );

            try (Socket socket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                String line;
                while ((line = in.readLine()) != null) {
                    out.println(processor.processCommand(line));
                }
            } catch (IOException ignored) {
            }
        }

        @Override
        public void close() throws Exception {
            serverSocket.close();
            serverThread.join(1000);
        }
    }
}
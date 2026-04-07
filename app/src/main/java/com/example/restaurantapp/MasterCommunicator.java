package com.example.restaurantapp;

import org.json.JSONObject;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;
import android.util.Log;

public class MasterCommunicator {

    private String masterIp;
    private int masterPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public MasterCommunicator(String masterIp, int masterPort) {
        this.masterIp = masterIp;
        this.masterPort = masterPort;
    }

    public synchronized boolean connect() {
        Log.d("MasterCommunicator", "connect() called");
        try {
            close();
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(masterIp, masterPort), 5000);
            this.socket.setSoTimeout(5000);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("CLIENT_HELLO");
            out.flush();
            // Read and discard the SERVER_HELLO response
            String response = in.readLine();
            Log.d("MasterCommunicator", "Server responded: " + response);
            Log.d("MasterCommunicator", "Connected to server at " + masterIp + ":" + masterPort);
            return true;
        } catch (IOException e) {
            Log.e("MasterCommunicator", "Error: Could not connect to Master at " + e.getMessage());
            close();
            return false;
        }
    }

    public synchronized boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed() && out != null && in != null;
    }

    public synchronized void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            Log.e("MasterCommunicator", "Error closing connection to Master: " + e.getMessage());
        } finally {
            out = null;
            in = null;
            socket = null;
        }
    }

    private synchronized String sendRequestAndGetResponse(String request) {
        // Try to reconnect if not connected
        if (!isConnected()) {
            Log.w("MasterCommunicator", "Not connected, attempting to reconnect...");
            if (!connect()) {
                Log.e("MasterCommunicator", "Reconnection failed");
                return null;
            }
            Log.d("MasterCommunicator", "Reconnected successfully");
        }
        
        try {
            out.println(request);
            out.flush();
            Log.d("MasterCommunicator", "Sent request to master: " + request);
            String response = in.readLine();
            if (response == null) {
                // Connection was closed, try to reconnect and retry once
                Log.w("MasterCommunicator", "Got null response, reconnecting...");
                if (connect()) {
                    out.println(request);
                    out.flush();
                    response = in.readLine();
                }
            }
            return response;
        } catch (IOException e) {
            Log.e("MasterCommunicator", "Error communicating with Master: " + e.getMessage());
            // Try to reconnect and retry once
            try {
                Log.w("MasterCommunicator", "Attempting reconnect after error...");
                if (connect()) {
                    out.println(request);
                    out.flush();
                    return in.readLine();
                }
            } catch (IOException e2) {
                Log.e("MasterCommunicator", "Retry also failed: " + e2.getMessage());
            }
            return null;
        }
    }

    public boolean sendAddStoreRequest(Store store) {
        String response = sendAddStoreRequestDetailed(store);
        return response != null && response.toLowerCase(Locale.ROOT).startsWith("ok");
    }

    public String sendAddStoreRequestDetailed(Store store) {
        String request = "ADD_STORE:" + store.toJson().toString().replace("\n", "").replace("\r", "");
        return sendRequestAndGetResponse(request);
    }

    public String sendSearchRequest(String latitude, String longitude, String foodCategory, String stars, String priceRange) {
        latitude = (latitude == null) ? "" : latitude;
        longitude = (longitude == null) ? "" : longitude;
        foodCategory = (foodCategory == null) ? "" : foodCategory;
        stars = (stars == null) ? "" : stars;
        priceRange = (priceRange == null) ? "" : priceRange;

        // Compose colon-separated string
        String request = String.format("SEARCH:%s:%s:%s:%s:%s", latitude, longitude, foodCategory, stars, priceRange);

        return sendRequestAndGetResponse(request);
    }

    public boolean sendRemoveStoreRequest(String storeName) {
        String response = sendRemoveStoreRequestDetailed(storeName);
        return response != null && response.toLowerCase(Locale.ROOT).startsWith("ok");
    }

    public String sendRemoveStoreRequestDetailed(String storeName) {
        String request = "REMOVE_STORE:" + storeName;
        return sendRequestAndGetResponse(request);
    }

    public boolean sendBuyRequest(String storeName, String productName, int quantity) {
        String response = sendBuyRequestDetailed(storeName, productName, quantity);
        return response != null && response.toLowerCase(Locale.ROOT).startsWith("success");
    }

    public String sendBuyRequestDetailed(String storeName, String productName, int quantity) {
        String request = "BUY:" + storeName + ":" + productName + ":" + quantity;
        return sendRequestAndGetResponse(request);
    }

    public boolean sendAddProductRequest(String storeName, Product product) {
        String response = sendAddProductRequestDetailed(storeName, product);
        return response != null && response.toLowerCase(Locale.ROOT).startsWith("ok");
    }

    public String sendAddProductRequestDetailed(String storeName, Product product) {
        try {
            JSONObject productJson = product.toJson();
            String request = "ADD_PRODUCT:" + storeName + ":" + productJson.toString();
            return sendRequestAndGetResponse(request);
        } catch (JSONException e) {
            Log.e("MasterCommunicator", "Error creating JSON for product: " + e.getMessage());
            return null;
        }
    }

    public boolean sendRemoveProductRequest(String storeName, String productName) {
        String response = sendRemoveProductRequestDetailed(storeName, productName);
        return response != null && response.toLowerCase(Locale.ROOT).startsWith("ok");
    }

    public String sendRemoveProductRequestDetailed(String storeName, String productName) {
        String request = "REMOVE_PRODUCT:" + storeName + ":" + productName;
        return sendRequestAndGetResponse(request);
    }

    public boolean sendUpdateProductRequest(String storeName, String productName, double newPrice, int newAmount) {
        String response = sendUpdateProductRequestDetailed(storeName, productName, newPrice, newAmount);
        return response != null && response.toLowerCase(Locale.ROOT).startsWith("ok");
    }

    public String sendUpdateProductRequestDetailed(String storeName, String productName, double newPrice, int newAmount) {
        String request = "UPDATE_PRODUCT:" + storeName + ":" + productName + ":" + newPrice + ":" + newAmount;
        return sendRequestAndGetResponse(request);
    }

    /**
     * Validate partner login credentials with the server
     * @return true if login is successful, false otherwise
     */
    public boolean sendPartnerLoginRequest(String storeName, String password) {
        String response = sendPartnerLoginRequestDetailed(storeName, password);
        return response != null && response.toLowerCase(Locale.ROOT).startsWith("ok");
    }

    public String sendPartnerLoginRequestDetailed(String storeName, String password) {
        String request = "PARTNER_LOGIN:" + storeName + ":" + password;
        String response = sendRequestAndGetResponse(request);
        Log.d("MasterCommunicator", "Partner login response: " + response);
        return response;
    }

    public String requestPartnerAccessCode(String storeName) {
        String request = "REQUEST_PARTNER_ACCESS_CODE:" + storeName;
        String response = sendRequestAndGetResponse(request);
        Log.d("MasterCommunicator", "Partner access code response: " + response);
        return response;
    }

    /**
     * Get credentials for a store (for demo purposes only)
     * In production, this would be replaced with a proper authentication system
     */
    public String getStoreCredentials(String storeName) {
        String request = "GET_CREDENTIALS:" + storeName;
        String response = sendRequestAndGetResponse(request);
        if (response != null && response.startsWith("PASSWORD:")) {
            return response.substring("PASSWORD:".length());
        }
        return null;
    }

}
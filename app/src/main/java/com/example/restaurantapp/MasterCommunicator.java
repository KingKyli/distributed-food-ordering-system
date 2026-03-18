package com.example.restaurantapp;

import org.json.JSONObject;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
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

    public boolean isConnected() {
        return socket != null
                && socket.isConnected()
                && !socket.isClosed()
                && out != null
                && in != null;
    }

    public boolean connect() {
        Log.d("MasterCommunicator", "connect() called");
        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(masterIp, masterPort), 5000); // 5 second timeout
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("CLIENT_HELLO");
            out.flush();
            Log.d("MasterCommunicator", "Connected to server at " + masterIp + ":" + masterPort);
            return true;
        } catch (IOException e) {
            Log.e("MasterCommunicator", "Error: Could not connect to Master at " + e.getMessage());
            close();
            return false;
        }
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            Log.e("MasterCommunicator", "Error closing connection to Master: " + e.getMessage());
        }
    }

    private synchronized String sendRequestAndGetResponse(String request) {
        if (!isConnected()) {
            Log.e("MasterCommunicator", "Not connected to server");
            return null;
        }
        try {
            out.println(request);
            out.flush();
            Log.d("MasterCommunicator", "Sent request to master: " + request);
            return in.readLine(); // Read response
        } catch (IOException e) {
            Log.e("MasterCommunicator", "Error communicating with Master: " + e.getMessage());
            return null;
        }
    }

    public boolean sendAddStoreRequest(Store store) {
        String request = "ADD_STORE:" + store.toJson().toString().replace("\n", "").replace("\r", "");
        String response = sendRequestAndGetResponse(request);
        return response != null && response.toLowerCase().startsWith("ok");
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
        String request = "REMOVE_STORE:" + storeName;
        String response = sendRequestAndGetResponse(request);
        return response != null && response.toLowerCase().startsWith("ok");
    }

    public boolean sendBuyRequest(String storeName, String productName, int quantity) {
        String request = "BUY:" + storeName + ":" + productName + ":" + quantity;
        String response = sendRequestAndGetResponse(request);
        return response != null && response.toLowerCase().startsWith("success");
    }

    public boolean sendAddProductRequest(String storeName, Product product) {
        try {
            JSONObject productJson = product.toJson();
            String request = "ADD_PRODUCT:" + storeName + ":" + productJson.toString();
            String response = sendRequestAndGetResponse(request);
            return response != null && response.toLowerCase().startsWith("ok");
        } catch (JSONException e) {
            Log.e("MasterCommunicator", "Error creating JSON for product: " + e.getMessage());
            return false;
        }
    }

    public boolean sendRemoveProductRequest(String storeName, String productName) {
        String request = "REMOVE_PRODUCT:" + storeName + ":" + productName;
        String response = sendRequestAndGetResponse(request);
        return response != null && response.toLowerCase().startsWith("ok");
    }

    public boolean sendUpdateProductRequest(String storeName, String productName, double newPrice, int newAmount) {
        String request = "UPDATE_PRODUCT:" + storeName + ":" + productName + ":" + newPrice + ":" + newAmount;
        String response = sendRequestAndGetResponse(request);
        return response != null && response.toLowerCase().startsWith("ok");
    }

}
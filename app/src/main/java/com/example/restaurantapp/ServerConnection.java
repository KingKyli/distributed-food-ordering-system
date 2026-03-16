package com.example.restaurantapp;

public class ServerConnection {
    private static MasterCommunicator communicator;

    public static void init(String ip, int port) {
        if (communicator == null) {
            communicator = new MasterCommunicator(ip, port);
            communicator.connect(); // You now call connect() here
        }
    }

    public static MasterCommunicator getInstance() {
        return communicator;
    }

    public static void close() {
        if (communicator != null) {
            communicator.close();
            communicator = null;
        }
    }
}

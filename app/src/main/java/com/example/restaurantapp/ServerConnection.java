package com.example.restaurantapp;

public class ServerConnection {
    private static volatile MasterCommunicator communicator;
    private static final Object lock = new Object();

    public static void init(String ip, int port) {
        synchronized (lock) {
            if (communicator == null) {
                MasterCommunicator newComm = new MasterCommunicator(ip, port);
                newComm.connect();
                communicator = newComm;
            }
        }
    }

    public static MasterCommunicator getInstance() {
        return communicator;
    }

    public static void close() {
        synchronized (lock) {
            if (communicator != null) {
                communicator.close();
                communicator = null;
            }
        }
    }
}

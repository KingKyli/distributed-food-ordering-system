package com.example.restaurantapp;

public class ServerConnection {
    private static volatile MasterCommunicator communicator;
    private static final Object lock = new Object();

    public static boolean init(String ip, int port) {
        synchronized (lock) {
            if (communicator == null) {
                MasterCommunicator newComm = new MasterCommunicator(ip, port);
                boolean ok = newComm.connect();
                if (ok) {
                    communicator = newComm;
                    return true;
                }
                return false;
            }
            return communicator.isConnected();
        }
    }

    public static MasterCommunicator getInstance() {
        return communicator;
    }

    public static boolean isConnected() {
        MasterCommunicator comm = communicator;
        return comm != null && comm.isConnected();
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

package com.example.restaurantapp;

import android.util.Log;

public class ServerConnection {
    private static final String TAG = "ServerConnection";
    private static MasterCommunicator communicator;
    private static String lastError = "Connection has not been initialized.";
    private static String lastSuccessfulIp;
    private static int lastSuccessfulPort = -1;

    public static synchronized boolean init(String ip, int port) {
        Log.d(TAG, "init() called with ip=" + ip + ", port=" + port);
        if (communicator != null && communicator.isConnected()) {
            Log.d(TAG, "Already connected, reusing connection");
            return true;
        }

        close();
        communicator = new MasterCommunicator(ip, port);
        boolean connected = communicator.connect();
        if (connected) {
            lastError = null;
            lastSuccessfulIp = ip;
            lastSuccessfulPort = port;
            Log.d(TAG, "Connection successful!");
            return true;
        }

        lastError = "Could not connect to server at " + ip + ":" + port;
        Log.e(TAG, lastError);
        communicator.close();
        communicator = null;
        return false;
    }

    public static synchronized MasterCommunicator getInstance() {
        Log.d(TAG, "getInstance() called, communicator=" + (communicator != null) + ", isConnected=" + (communicator != null && communicator.isConnected()));
        if (communicator == null || !communicator.isConnected()) {
            Log.w(TAG, "getInstance() detected a disconnected communicator. Trying to recover connection.");
            if (!ensureReady()) {
                Log.e(TAG, "getInstance() returning null - not connected");
                return null;
            }
        }
        return communicator;
    }

    public static synchronized AppResult<MasterCommunicator> requireCommunicator() {
        if (ensureReady() && communicator != null && communicator.isConnected()) {
            return AppResult.success(communicator);
        }
        String message = lastError;
        if (message == null || message.trim().isEmpty()) {
            message = "Server connection is not available.";
        }
        return AppResult.error(message);
    }

    public static synchronized boolean isReady() {
        boolean ready = communicator != null && communicator.isConnected();
        Log.d(TAG, "isReady() = " + ready);
        return ready;
    }

    public static synchronized boolean ensureReady() {
        if (isReady()) {
            return true;
        }

        if (lastSuccessfulIp == null || lastSuccessfulIp.trim().isEmpty() || lastSuccessfulPort <= 0) {
            lastError = "Connection is not available and there is no previous server to reconnect to.";
            Log.e(TAG, lastError);
            return false;
        }

        Log.w(TAG, "Connection dropped. Attempting to reconnect to " + lastSuccessfulIp + ":" + lastSuccessfulPort);
        return init(lastSuccessfulIp, lastSuccessfulPort);
    }

    public static synchronized String getLastError() {
        return lastError;
    }

    public static synchronized void close() {
        Log.d(TAG, "close() called");
        if (communicator != null) {
            communicator.close();
            communicator = null;
        }
    }
}

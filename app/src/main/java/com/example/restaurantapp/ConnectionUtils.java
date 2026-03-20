package com.example.restaurantapp;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @deprecated Connection-checking and redirect logic is now handled exclusively by
 * {@link ActivityUtils#ensureConnectedOrRedirect(AppCompatActivity)}.
 * This class is kept only for source-level backward compatibility and delegates
 * its sole method to {@code ActivityUtils}. No new callers should use it.
 */
@Deprecated
public final class ConnectionUtils {
    private ConnectionUtils() {
    }

    /**
     * @deprecated Use {@link ActivityUtils#ensureConnectedOrRedirect(AppCompatActivity)} instead.
     */
    @Deprecated
    public static MasterCommunicator requireConnected(Activity activity) {
        if (!(activity instanceof AppCompatActivity)) {
            return null;
        }
        boolean ready = ActivityUtils.ensureConnectedOrRedirect((AppCompatActivity) activity);
        return ready ? ServerConnection.getInstance() : null;
    }
}

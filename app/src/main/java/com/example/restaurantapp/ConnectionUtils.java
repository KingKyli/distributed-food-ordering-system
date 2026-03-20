package com.example.restaurantapp;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

/**
 * @deprecated Use {@link ActivityUtils#ensureConnectedOrRedirect(androidx.appcompat.app.AppCompatActivity)}
 * and {@link ServerConnection#requireCommunicator()} instead.
 * This class is kept only for backward compatibility and will not be removed,
 * but it is no longer part of the active design.
 */
@Deprecated
public final class ConnectionUtils {
    private ConnectionUtils() {
    }

    public static MasterCommunicator requireConnected(Activity activity) {
        MasterCommunicator comm = ServerConnection.getInstance();
        if (comm != null && comm.isConnected()) {
            return comm;
        }

        Toast.makeText(activity, "Not connected to server. Please connect first.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(activity, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
        return null;
    }
}

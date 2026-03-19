package com.example.restaurantapp;

import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public final class ActivityUtils {
    private ActivityUtils() {
    }

    public static boolean isActivityAlive(AppCompatActivity activity) {
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }

    public static void runOnUiThreadIfAlive(AppCompatActivity activity, Runnable action) {
        if (!isActivityAlive(activity)) {
            return;
        }
        activity.runOnUiThread(() -> {
            if (isActivityAlive(activity)) {
                action.run();
            }
        });
    }

    public static boolean ensureConnectedOrRedirect(AppCompatActivity activity) {
        if (ServerConnection.ensureReady()) {
            return true;
        }

        if (isPartnerFlowActivity(activity)) {
            if (isActivityAlive(activity)) {
                String message = PartnerSessionStore.hasActiveSession(activity)
                        ? "Connection lost. Restoring your partner session with cached data."
                        : "Server connection is unavailable. Stay on this screen and retry when the server is back.";
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
            return true;
        }

        if (isActivityAlive(activity)) {
            Toast.makeText(activity, "Server connection lost. Returning to welcome screen.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(activity, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            activity.finish();
        }
        return false;
    }

    private static boolean isPartnerFlowActivity(AppCompatActivity activity) {
        return activity instanceof PartnerLoginActivity
                || activity instanceof ManagerConsoleActivity
                || activity instanceof AddProductActivity
                || activity instanceof EditProductActivity
                || activity instanceof ProductEditActivity;
    }
}


package com.example.restaurantapp;

import java.util.Locale;

public final class ProtocolUtils {
    private ProtocolUtils() {
    }

    public static boolean isOkResponse(String response) {
        if (response == null) {
            return false;
        }
        String normalized = response.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("ok") || normalized.startsWith("success");
    }

    public static String extractErrorMessage(String response, String fallback) {
        if (response == null || response.trim().isEmpty()) {
            return fallback;
        }
        String trimmed = response.trim();
        if (trimmed.regionMatches(true, 0, "ERROR:", 0, "ERROR:".length())) {
            String message = trimmed.substring("ERROR:".length()).trim();
            return message.isEmpty() ? fallback : message;
        }
        return trimmed;
    }
}

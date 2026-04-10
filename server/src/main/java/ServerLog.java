import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

final class ServerLog {
    private static final String LOG_LEVEL_ENV = "SERVER_LOG_LEVEL";
    private static volatile boolean configured;

    private ServerLog() {
    }

    static Logger getLogger(Class<?> type) {
        configureOnce();
        return Logger.getLogger(type.getName());
    }

    private static synchronized void configureOnce() {
        if (configured) {
            return;
        }

        Level configuredLevel = parseLevel(System.getenv(LOG_LEVEL_ENV));
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(configuredLevel);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(configuredLevel);
        }

        configured = true;
    }

    private static Level parseLevel(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Level.INFO;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "SEVERE":
                return Level.SEVERE;
            case "WARNING":
                return Level.WARNING;
            case "INFO":
                return Level.INFO;
            case "CONFIG":
                return Level.CONFIG;
            case "FINE":
                return Level.FINE;
            case "FINER":
                return Level.FINER;
            case "FINEST":
                return Level.FINEST;
            default:
                return Level.INFO;
        }
    }
}
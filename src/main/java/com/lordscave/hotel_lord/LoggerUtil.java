package com.lordscave.hotel_lord;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtil {
    private static final Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());


    private static boolean enableDatabaseLogging = false;

    public static void setDatabaseLoggingEnabled(boolean enabled) {
        enableDatabaseLogging = enabled;
    }

    public static void log(Level level, String message, Throwable thrown) {
        try {
            LOGGER.log(level, message, thrown);

            if (enableDatabaseLogging) {
                DatabaseLogger.log(level, message, thrown);
            }
        } catch (Exception e) {
            fallbackFileLogging(e);
        }
    }

    public static void log(Level level, String message) {
        try {
            LOGGER.log(level, message);

            if (enableDatabaseLogging) {
                DatabaseLogger.log(level, message);
            }
        } catch (Exception e) {
            fallbackFileLogging(e);
        }
    }

    private static void fallbackFileLogging(Exception e) {
        try (java.io.FileWriter fw = new java.io.FileWriter("log_errors.txt", true)) {
            fw.write(java.time.LocalDateTime.now() + " - Logging Failed: " + e.getMessage() + "\n");
        } catch (Exception ignored) {

        }
    }
}

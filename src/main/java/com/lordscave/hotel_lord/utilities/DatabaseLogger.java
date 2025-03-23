package com.lordscave.hotel_lord.utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.logging.Level;

public class DatabaseLogger {
    public static void log(Level level, String message, Throwable thrown) {
        String sql = "INSERT INTO app_logs (log_time, level, message, exception) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, level.getName());
            stmt.setString(3, message);
            stmt.setString(4, (thrown != null) ? thrown.toString() : null);
            stmt.executeUpdate();
        } catch (Exception e) {
            fallbackFileLogging(e);
        }
    }

    public static void log(Level level, String message) {
        log(level, message, null);
    }

    private static void fallbackFileLogging(Exception e) {
        try (java.io.FileWriter fw = new java.io.FileWriter("log_errors.txt", true)) {
            fw.write(new Timestamp(System.currentTimeMillis()) + " - Logging Failed: " + e.getMessage() + "\n");
        } catch (Exception ignored) {

        }
    }
}

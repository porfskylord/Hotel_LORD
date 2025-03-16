package com.lordscave.hotel_lord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/HotelLord";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "azad@1234";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            LoggerUtil.log(Level.SEVERE, "Database connection error");
            return null;
        }
    }

    public static ResultSet executeQuery(String query, Object... params) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            if (conn == null) {
                LoggerUtil.log(Level.SEVERE, "Failed to execute query: No database connection.");
                return null;
            }

            stmt = conn.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            rs = stmt.executeQuery();
            return rs;

        } catch (SQLException e) {
            LoggerUtil.log(Level.SEVERE, "SQL Query Execution Failed: " + query);
        }

        return null;
    }

    public static int executeUpdate(String query, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn != null ? conn.prepareStatement(query) : null) {

            if (stmt == null) {
                LoggerUtil.log(Level.SEVERE, "Failed to execute update: No database connection.");
                return -1;
            }

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeUpdate();

        } catch (SQLException e) {
            LoggerUtil.log(Level.SEVERE, "SQL Update Execution Failed: " + query);
        }
        return -1;
    }

    public static List<MenuItem> getMenuItems() {
        List<MenuItem> menuList = new ArrayList<>();
        String query = "SELECT menu_name, fxml_path FROM app_navigation ORDER BY id";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn != null ? conn.prepareStatement(query) : null;
             ResultSet rs = stmt != null ? stmt.executeQuery() : null) {

            if (rs == null) {
                LoggerUtil.log(Level.SEVERE, "Failed to retrieve menu items: No database connection.");
                return menuList;
            }

            while (rs.next()) {
                menuList.add(new MenuItem(rs.getString("menu_name"), rs.getString("fxml_path")));
            }
        } catch (SQLException e) {
            LoggerUtil.log(Level.SEVERE, "Failed to fetch menu items from database");
        }

        return menuList;
    }

    public static boolean updateUserPassword(String email, String newPassword) {
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        String query = "UPDATE hotel_staff SET password = ?, otp = NULL WHERE email = ?";
        return executeUpdate(query, hashedPassword, email) > 0;
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                LoggerUtil.log(Level.INFO, "✅ Database connected successfully!");

                List<MenuItem> menuItems = DatabaseConnection.getMenuItems();
                for (MenuItem item : menuItems) {
                    LoggerUtil.log(Level.INFO, "Loaded menu item: " + item.getName());
                }
            } else {
                LoggerUtil.log(Level.SEVERE, "❌ Failed to connect to the database.");
            }
        } catch (Exception e) {
            LoggerUtil.log(Level.SEVERE, "❌ Database connection error");
        }
    }
}

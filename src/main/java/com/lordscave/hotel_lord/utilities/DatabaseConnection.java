package com.lordscave.hotel_lord.utilities;

import com.lordscave.hotel_lord.Entities.MenuItem;

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

    public static List<Object[]> executeQuery(String query, Object... params) {
        List<Object[]> resultList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn != null ? conn.prepareStatement(query) : null) {

            if (stmt == null) return resultList;

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                int columnCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.log(Level.SEVERE, "SQL Query Execution Failed: " + query, e);
        }

        return resultList;
    }

    public static int executeUpdate(String query, Object... params) {
        try (Connection conn = getConnection()) {
            if (conn == null) return -1;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            LoggerUtil.log(Level.SEVERE, "SQL Update Execution Failed: " + query, e);
        }
        return -1;
    }

    public static List<MenuItem> getMenuItems() {
        List<MenuItem> menuList = new ArrayList<>();
        String query = "SELECT menu_name, fxml_path, icon_path FROM app_navigation WHERE isactive = true ORDER BY id";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn != null ? conn.prepareStatement(query) : null;
             ResultSet rs = stmt != null ? stmt.executeQuery() : null) {

            if (rs == null) return menuList;

            while (rs.next()) {
                menuList.add(new MenuItem(rs.getString("menu_name"), rs.getString("fxml_path"), rs.getString("icon_path")));
            }
        } catch (SQLException e) {
            LoggerUtil.log(Level.SEVERE, "Error retrieving menu items from database", e);
        }

        return menuList;
    }

    public static boolean updateUserPassword(String email, String newPassword) {
        String query = "UPDATE hotel_staff SET password = ?, otp = NULL WHERE email = ?";

        if (!emailExists(email)) {
            LoggerUtil.log(Level.WARNING, "Email not found: " + email);
            return false;
        }

        return executeUpdate(query, newPassword, email) > 0;
    }

    private static boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM hotel_staff WHERE email = ?";
        List<Object[]> result = executeQuery(query, email);
        return !result.isEmpty() && ((long) result.get(0)[0]) > 0;
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                LoggerUtil.log(Level.INFO, "✅ Database connected successfully!");
                List<MenuItem> menuItems = getMenuItems();
                for (MenuItem item : menuItems) {
                    LoggerUtil.log(Level.INFO, "Loaded menu item: " + item.getName());
                }
            } else {
                LoggerUtil.log(Level.SEVERE, "❌ Failed to connect to the database.");
            }
        } catch (Exception e) {
            LoggerUtil.log(Level.SEVERE, "❌ Database connection error", e);
        }
    }
}

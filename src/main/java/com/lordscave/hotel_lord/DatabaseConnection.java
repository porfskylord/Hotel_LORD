package com.lordscave.hotel_lord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/HotelLord";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "azad@1234";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Executes a SELECT query and returns the ResultSet.
     * The caller must close the ResultSet after use.
     *
     * @param query  SQL query to execute (should be a SELECT statement)
     * @param params Parameters for the query (if any)
     * @return ResultSet containing query results (must be closed by the caller)
     */
    public static ResultSet executeQuery(String query, Object... params) {
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeQuery(); // Return ResultSet, caller must close it
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Executes an INSERT, UPDATE, or DELETE query.
     *
     * @param query  SQL query to execute (should NOT be a SELECT statement)
     * @param params Parameters for the query (if any)
     * @return Number of affected rows (returns -1 if an error occurs)
     */
    public static int executeUpdate(String query, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Fetches menu items from the database.
     *
     * @return List of menu items
     */
    public static List<MenuItem> getMenuItems() {
        List<MenuItem> menuList = new ArrayList<>();
        String query = "SELECT menu_name, fxml_path FROM comn_menu order by id";

        try (ResultSet rs = executeQuery(query)) {
            while (rs != null && rs.next()) {
                menuList.add(new MenuItem(rs.getString("menu_name"), rs.getString("fxml_path")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return menuList;
    }

    /**
     * Updates the user's password.
     *
     * @param email       User's email
     * @param newPassword New hashed password
     * @return true if update is successful, false otherwise
     */
    public static boolean updateUserPassword(String email, String newPassword) {
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        String query = "UPDATE sec_users SET password = ?, otp = NULL WHERE email = ?";
        return executeUpdate(query, hashedPassword, email) > 0;
    }

    public static void main(String[] args) {
        // Test database connection
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ Database connected successfully!");

                List<MenuItem> menuItems = DatabaseConnection.getMenuItems();
                for (MenuItem item : menuItems) {
                    System.out.println(item.getName());
                }
            } else {
                System.out.println("❌ Failed to connect to the database.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Database connection error: " + e.getMessage());
        }
    }
}

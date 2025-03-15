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


    public static ResultSet executeQuery(String query, Object... params) {
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

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


    public static boolean updateUserPassword(String email, String newPassword) {
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        String query = "UPDATE sec_users SET password = ?, otp = NULL WHERE email = ?";
        return executeUpdate(query, hashedPassword, email) > 0;
    }

    public static void main(String[] args) {
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

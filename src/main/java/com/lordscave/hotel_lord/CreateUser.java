package com.lordscave.hotel_lord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateUser {

    public static void main(String[] args) {
        String username = "a"; 
        String rawPassword = "1";
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);

        if (storeUser(username, hashedPassword)) {
            System.out.println("User created successfully!");
        } else {
            System.out.println("Failed to create user.");
        }
    }

    public static boolean storeUser(String username, String hashedPassword) {
        String sql = "INSERT INTO sec_users (username, password) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

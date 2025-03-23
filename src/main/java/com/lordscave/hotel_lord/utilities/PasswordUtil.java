package com.lordscave.hotel_lord.utilities;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final int BCRYPT_COST = 12; // Adjust cost based on performance needs

    // Hash a password using BCrypt
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
    }

    // Verify a password against a stored hash
    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    public static void main(String[] args) {
        String rawPassword = "azad0110";

        // Simulate stored hash from app
        String storedHash = "$2a$12$oZgaFoHMZWkiKybN8uEfI.RFTjQGT7CTJ8icYnyqLqivdScekwa6m";

        // Verify password against the stored hash
        boolean match = BCrypt.checkpw(rawPassword, storedHash);

        System.out.println("Password Match: " + match); // Should print: true
    }
}

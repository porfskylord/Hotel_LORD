package com.lordscave.hotel_lord;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Hash password before storing in DB
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Verify user input password against stored hash
    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}

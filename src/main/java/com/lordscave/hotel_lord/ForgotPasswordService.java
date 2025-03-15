package com.lordscave.hotel_lord;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;

public class ForgotPasswordService {

    public static void processForgotPassword(String username) {
        String email = getUserEmail(username);

        if (email == null) {
            Platform.runLater(() -> CustomAlert.show("Error", "No account found for this username."));
            return;
        }

        // Show loader in UI thread
        LoaderPopup loader = new LoaderPopup();
        Platform.runLater(loader::show);

        // Run OTP generation & sending in a background thread
        new Thread(() -> {
            String otp = generateOTP();

            if (!storeOTPInDatabase(email, otp)) {
                Platform.runLater(() -> {
                    loader.close();
                    CustomAlert.show("Error", "Failed to generate OTP. Please try again.");
                });
                return;
            }

            if (!sendOTPEmail(email, otp)) { // Ensure sendOTPEmail uses Platform.runLater internally
                Platform.runLater(() -> {
                    loader.close();
                    CustomAlert.show("Error", "Failed to send OTP email. Please try again.");
                });
                return;
            }

            // Close loader and show OTP verification
            loader.close();

        }).start();
    }




    // 1️⃣ Get User Email from sec_users table
    private static String getUserEmail(String username) {
        String sql = "SELECT email FROM sec_users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("email");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2️⃣ Generate a 6-digit OTP
    private static String generateOTP() {
        Random rand = new Random();
        return String.format("%06d", rand.nextInt(1000000));
    }

    // 3️⃣ Store OTP in Database
    private static boolean storeOTPInDatabase(String email, String otp) {
        String sql = "UPDATE sec_users SET otp = ? WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, otp);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 4️⃣ Send OTP Email - Returns true if successful
    private static boolean sendOTPEmail(String recipientEmail, String otp) {
        final String senderEmail = "hotellord001@gmail.com"; // Use a verified sender email
        final String senderPassword = "hfyq ekby momd sfuw"; // Use an App Password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your OTP for Password Reset");
            message.setText("Your OTP is: " + otp + "\n\nDo not share this code with anyone.");

            Transport.send(message);
            // ✅ Show alert and wait for the user to click "OK"
            Platform.runLater(() -> {
                CustomAlert.showAndWait("Success", "OTP has been sent to your email (Please check Spam too).");
                // ✅ Only after clicking "OK", open the reset password screen
                verifyAndResetPassword(recipientEmail, otp);
            });

            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }


    // 5️⃣ Verify OTP and Allow Password Reset
    private static void verifyAndResetPassword(String email, String originalOtp) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(ForgotPasswordService.class.getResource("ResetPassword.fxml"));
                Parent root = loader.load();

                // Get the controller instance and pass the email & OTP
                ResetPasswordController controller = loader.getController();
                controller.setUserEmail(email);
                controller.setOriginalOtp(originalOtp);

                // Create a new stage
                Stage stage = new Stage();
                Scene scene = new Scene(root, 465, 270);
                scene.setFill(Color.TRANSPARENT);
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.setScene(scene);
                stage.setTitle("Reset Password");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();

                // ✅ Ensure alert is shown on the JavaFX thread
                Platform.runLater(() -> CustomAlert.show("Error", "Failed to load Reset Password screen."));
            }
        });
    }








}

package com.lordscave.hotel_lord;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;

public class ForgotPasswordService {

    public static void processForgotPassword(String username) {
        String email = getUserEmail(username);

        if (email == null) {
            Platform.runLater(() -> CustomAlert.show("Error", "No account found for this username."));
            return;
        }

        LoaderPopup loader = new LoaderPopup();
        Platform.runLater(loader::show);

        new Thread(() -> {
            try {
                String otp = generateOTP();

                if (!storeOTPInDatabase(email, otp)) {
                    LoggerUtil.log(Level.WARNING, "Failed to store OTP in database for user: " + username);
                    Platform.runLater(() -> {
                        loader.close();
                        CustomAlert.show("Error", "Failed to generate OTP. Please try again.");
                    });
                    return;
                }

                if (!sendOTPEmail(email, otp)) {
                    LoggerUtil.log(Level.WARNING, "Failed to send OTP email to: " + email);
                    Platform.runLater(() -> {
                        loader.close();
                        CustomAlert.show("Error", "Failed to send OTP email. Please try again.");
                    });
                    return;
                }

                Platform.runLater(() -> {
                    loader.close();
                    CustomAlert.showAndWait("Success", "OTP has been sent to your email (Check Spam too).");
                    verifyAndResetPassword(email, otp);
                });

            } catch (Exception e) {
                LoggerUtil.log(Level.SEVERE, "Unexpected error in ForgotPassword process for user: " + username, e);
                Platform.runLater(() -> {
                    loader.close();
                    CustomAlert.show("Error", "An unexpected error occurred. Please try again.");
                });
            }
        }).start();
    }

    private static String getUserEmail(String username) {
        String sql = "SELECT email FROM hotel_staff WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("email");
        } catch (Exception e) {
            LoggerUtil.log(Level.SEVERE, "Error fetching email for username: " + username);
        }
        return null;
    }

    private static String generateOTP() {
        Random rand = new Random();
        return String.format("%06d", rand.nextInt(1000000));
    }

    private static boolean storeOTPInDatabase(String email, String otp) {
        String sql = "UPDATE hotel_staff SET otp = ? WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, otp);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            LoggerUtil.log(Level.SEVERE, "Error storing OTP in database for email: " + email);
        }
        return false;
    }

    private static boolean sendOTPEmail(String recipientEmail, String otp) {
        final String senderEmail = "hotellord001@gmail.com";
        final String senderPassword = "hfyq ekby momd sfuw";

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
            LoggerUtil.log(Level.INFO, "OTP email sent successfully to: " + recipientEmail);
            return true;

        } catch (MessagingException e) {
            LoggerUtil.log(Level.SEVERE, "Failed to send OTP email to: " + recipientEmail);
            return false;
        }
    }

    private static void verifyAndResetPassword(String email, String originalOtp) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(ForgotPasswordService.class.getResource("ResetPassword.fxml"));
                Parent root = loader.load();

                ResetPasswordController controller = loader.getController();
                controller.setUserEmail(email);
                controller.setOriginalOtp(originalOtp);

                Stage stage = new Stage();
                Scene scene = new Scene(root, 465, 270);
                scene.setFill(Color.TRANSPARENT);
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.setScene(scene);
                stage.setTitle("Reset Password");
                stage.show();

            } catch (IOException e) {
                LoggerUtil.log(Level.SEVERE, "Failed to load Reset Password screen for email: " + email);
                Platform.runLater(() -> CustomAlert.show("Error", "Failed to load Reset Password screen."));
            }
        });
    }
}

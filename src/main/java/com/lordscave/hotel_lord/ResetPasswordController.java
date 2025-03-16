package com.lordscave.hotel_lord;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import java.util.logging.Level;

public class ResetPasswordController {

    @FXML private TextField otpField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetButton;

    private String userEmail;
    private String originalOtp;

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public void setOriginalOtp(String otp) {
        this.originalOtp = otp;
    }

    @FXML
    private void handleResetPassword() {
        String enteredOtp = otpField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();


        if (enteredOtp.isEmpty()) {
            CustomAlert.show("Error", "Please enter OTP.");
            return;
        }

        if (!enteredOtp.equals(originalOtp)) {
            CustomAlert.show("Error", "Invalid OTP! Please check and try again.");
            LoggerUtil.log(Level.WARNING, "Invalid OTP attempt for email: " + userEmail);
            return;
        }


        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            CustomAlert.show("Error", "Please enter both password fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            CustomAlert.show("Error", "Passwords do not match!");
            return;
        }

        if (!isValidPassword(newPassword)) {
            CustomAlert.show("Error", "Password must be at least 8 characters long and contain a mix of letters and numbers.");
            return;
        }


        String hashedPassword = PasswordUtil.hashPassword(newPassword);


        if (updatePassword(userEmail, hashedPassword)) {
            CustomAlert.show("Success", "Password reset successfully!");
            LoggerUtil.log(Level.INFO, "Password reset successfully for email: " + userEmail);


            resetButton.getScene().getWindow().hide();
        } else {
            CustomAlert.show("Error", "Failed to update password. Try again.");
            LoggerUtil.log(Level.SEVERE, "Failed to reset password for email: " + userEmail);
        }
    }

    private boolean updatePassword(String email, String hashedPassword) {
        return DatabaseConnection.updateUserPassword(email, hashedPassword);
    }


    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }
}

package com.lordscave.hotel_lord;

import com.lordscave.hotel_lord.CustomAlert;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class ResetPasswordController {

    @FXML
    private TextField otpField;  // ✅ OTP field from FXML

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button resetButton;

    private String userEmail;  // ✅ User's email
    private String originalOtp;  // ✅ OTP received in email

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public void setOriginalOtp(String otp) {
        this.originalOtp = otp;  // ✅ Store OTP for validation
    }

    @FXML
    private void handleResetPassword() {
        String enteredOtp = otpField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (enteredOtp.isEmpty()) {
            CustomAlert.show("Error", "Please enter OTP.");
            return;
        }

        if (!enteredOtp.equals(originalOtp)) {
            CustomAlert.show("Error", "Invalid OTP! Please check and try again.");
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

        if (updatePassword(userEmail, newPassword)) {
            CustomAlert.show("Success", "Password reset successfully!");
            resetButton.getScene().getWindow().hide(); // Close the window
        } else {
            CustomAlert.show("Error", "Failed to update password. Try again.");
        }
    }

    private boolean updatePassword(String email, String newPassword) {
        // Implement your database logic here
        return DatabaseConnection.updateUserPassword(email, newPassword);
    }
}

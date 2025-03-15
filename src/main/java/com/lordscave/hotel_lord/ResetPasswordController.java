package com.lordscave.hotel_lord;

import com.lordscave.hotel_lord.CustomAlert;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class ResetPasswordController {

    @FXML
    private TextField otpField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button resetButton;

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
            resetButton.getScene().getWindow().hide();
        } else {
            CustomAlert.show("Error", "Failed to update password. Try again.");
        }
    }

    private boolean updatePassword(String email, String newPassword) {

        return DatabaseConnection.updateUserPassword(email, newPassword);
    }
}

package com.lordscave.hotel_lord;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class BookingController {

    public Button btnWalkinBooking;
    public Button btnCheckAvailability;
    public Button btnModifyBooking;
    public Button btnCancelBooking;
    @FXML private TextField txtGuestName;
    @FXML private TextField txtContactNumber;
    @FXML private ComboBox<String> cmbRoomType;
    @FXML private DatePicker dpCheckin;
    @FXML private DatePicker dpCheckout;
    @FXML private ComboBox<String> cmbBookingStatus;
    @FXML private TextField txtAdvancePayment;
    @FXML private ComboBox<String> cmbRefundPolicy;

    public void initialize() {
        cmbRoomType.getItems().addAll("Single", "Double", "Suite", "Deluxe");
        cmbBookingStatus.getItems().addAll("Pending", "Confirmed", "Cancelled");
        cmbRefundPolicy.getItems().addAll("Non-refundable", "50% Refund", "Full Refund");

        dpCheckin.setValue(LocalDate.now());
        dpCheckout.setValue(LocalDate.now().plusDays(1));
    }

    @FXML
    private void confirmBooking() {
        String guestName = txtGuestName.getText();
        String contact = txtContactNumber.getText();
        String roomType = cmbRoomType.getValue();
        LocalDate checkin = dpCheckin.getValue();
        LocalDate checkout = dpCheckout.getValue();
        String status = cmbBookingStatus.getValue();
        double advancePayment = Double.parseDouble(txtAdvancePayment.getText());
        String refundPolicy = cmbRefundPolicy.getValue();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO bookings (guest_name, contact, room_type, checkin_date, checkout_date, status, advance_payment, refund_policy) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, guestName);
            stmt.setString(2, contact);
            stmt.setString(3, roomType);
            stmt.setDate(4, java.sql.Date.valueOf(checkin));
            stmt.setDate(5, java.sql.Date.valueOf(checkout));
            stmt.setString(6, status);
            stmt.setDouble(7, advancePayment);
            stmt.setString(8, refundPolicy);
            stmt.executeUpdate();

            showAlert("Success", "Booking Confirmed Successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database Error: " + e.getMessage());
        }
    }

    @FXML
    private void printBookingConfirmation() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Booking Confirmation");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            showAlert("Success", "Booking Confirmation saved to " + file.getAbsolutePath());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

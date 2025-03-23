package com.lordscave.hotel_lord.Controllers;

import com.lordscave.hotel_lord.utilities.DatabaseConnection;
import com.lordscave.hotel_lord.utilities.SignatureCapture;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class BookingController {

    private static BookingController instance;
    @FXML private TextField guestNameField, emailField, contactNoField, ageField, genderField, checkinDateField, checkoutDateField, roomTypeField, idNumberField, totalAmountField, advancePaymentField, balanceDueField, remarksField;
    @FXML private TextArea addressField;
    @FXML private ComboBox<String> availableRoomsCombo, idTypeCombo;
    @FXML private CheckBox foodService, laundryService, wifiService;
    @FXML private Button refreshRoomsBtn, cancelCheckinBtn ,captureSignatureBtn, capturePhotoBtn;
    @FXML private ScrollPane scrollPane;

    private int room_Type_Id = 0, roomRate = 0;

    public BookingController() {
        instance = this;
    }

    public static BookingController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        scrollPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/lordscave/hotel_lord/static/styles.css")).toExternalForm());
        cancelCheckinBtn.setOnAction(event -> resetForm());
        refreshRoomsBtn.setOnAction(event -> loadAvailableRooms());
        advancePaymentField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) updateBalanceDue();
        });
        captureSignatureBtn.setOnAction(e -> {
            Stage stage = (Stage) captureSignatureBtn.getScene().getWindow();
            SignatureCapture.showSignaturePad(stage);
        });

    }

    @FXML
    public void handleRequestBookingNow() {
        openWindow("/com/lordscave/hotel_lord/NewBookingReq.fxml", "Request Booking Now", 700, 650);
    }

    @FXML
    public void handleOnlineBookingRequests() {
        openWindow("/com/lordscave/hotel_lord/OnlineBookingRequests.fxml", "Online Booking Requests", 700, 555);
    }

    private void openWindow(String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle(title);
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/lordscave/hotel_lord/static/styles.css")).toExternalForm());
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadApprovedBooking(String guestName, String email, String contactNo, String address, int age, String gender, LocalDate checkin, LocalDate checkout, String roomType, int roomTypeId, int ratePerNight) {
        guestNameField.setText(guestName);
        emailField.setText(email);
        contactNoField.setText(contactNo);
        ageField.setText(String.valueOf(age));
        genderField.setText(gender);
        checkinDateField.setText(String.valueOf(checkin));
        checkoutDateField.setText(String.valueOf(checkout));
        addressField.setText(address);
        roomTypeField.setText(roomType);
        room_Type_Id = roomTypeId;
        roomRate = ratePerNight;
        loadAvailableRooms();
        calculateTotalAmount();
    }

    private void calculateTotalAmount() {
        try {
            LocalDate checkin = LocalDate.parse(checkinDateField.getText());
            LocalDate checkout = LocalDate.parse(checkoutDateField.getText());
            long days = ChronoUnit.DAYS.between(checkin, checkout);
            if (days <= 0) days = 1;
            double totalAmount = days * roomRate;
            totalAmountField.setText(String.valueOf(totalAmount));
            balanceDueField.setText(String.valueOf(totalAmount));
        } catch (Exception e) {
            totalAmountField.setText("Error");
        }
    }

    private void updateBalanceDue() {
        try {
            double totalAmount = Double.parseDouble(totalAmountField.getText());
            double advancePayment = Double.parseDouble(advancePaymentField.getText());
            double balanceDue = totalAmount - advancePayment;
            if (balanceDue < 0) balanceDue = 0;
            balanceDueField.setText(String.valueOf(balanceDue));
        } catch (NumberFormatException e) {
            balanceDueField.setText("Invalid Input");
        }
    }

    private void loadAvailableRooms() {
        ObservableList<String> availableRooms = FXCollections.observableArrayList();
        String query = "SELECT roomno FROM hotel_rooms WHERE isbooked = false AND roomtypeid = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            assert conn != null;
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, room_Type_Id);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    availableRooms.add(rs.getString("roomno"));
                }
                availableRoomsCombo.setItems(availableRooms);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void resetForm() {
        guestNameField.clear();
        emailField.clear();
        contactNoField.clear();
        ageField.clear();
        genderField.clear();
        checkinDateField.clear();
        checkoutDateField.clear();
        roomTypeField.clear();
        addressField.clear();
        idNumberField.clear();
        totalAmountField.clear();
        advancePaymentField.clear();
        balanceDueField.clear();
        remarksField.clear();
        availableRoomsCombo.getSelectionModel().clearSelection();
        idTypeCombo.getSelectionModel().clearSelection();
        foodService.setSelected(false);
        laundryService.setSelected(false);
        wifiService.setSelected(false);
    }
}

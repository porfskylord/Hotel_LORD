package com.lordscave.hotel_lord.Controllers;

import com.lordscave.hotel_lord.Entities.OndeskBooking;
import com.lordscave.hotel_lord.utilities.CustomAlert;
import com.lordscave.hotel_lord.utilities.OndeskBookingDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BookingFormController {

    @FXML private HBox titleBar;
    @FXML private Button closeBtn, btnBookNow, btnCancel;
    @FXML private ComboBox<String> cmbGender, cmbRoomType;
    @FXML private TextField txtFullName, txtContactNumber, txtEmail, txtAge, txtGuests;
    @FXML private TextArea txtAddress;
    @FXML private DatePicker dpCheckin, dpCheckout;

    private double xOffset = 0, yOffset = 0;
    private final Map<String, Integer> roomTypeMap = new HashMap<>();

    @FXML
    public void initialize() {
        setupDraggableWindow();
        setupCloseButton();
        loadRoomTypes();
        cmbGender.getItems().addAll("Male", "Female", "Other");
    }

    private void setupCloseButton() {
        if (closeBtn != null) {
            closeBtn.setOnAction(event -> closeWindow());
        }
    }

    private void setupDraggableWindow() {
        if (titleBar != null) {
            titleBar.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            titleBar.setOnMouseDragged(event -> {
                Stage stage = (Stage) titleBar.getScene().getWindow();
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void loadRoomTypes() {
        ObservableList<Pair<Integer, String>> roomTypes = OndeskBookingDAO.loadAvailableRooms();
        cmbRoomType.getItems().clear();
        roomTypeMap.clear();
        if (roomTypes.isEmpty()) {
            cmbRoomType.setPromptText("No Rooms Available");
        } else {
            for (Pair<Integer, String> pair : roomTypes) {
                roomTypeMap.put(pair.getValue(), pair.getKey());
                cmbRoomType.getItems().add(pair.getValue());
            }
            cmbRoomType.setPromptText("Select Room Type");
        }
    }

    @FXML
    private void saveBooking() {
        String fullName = txtFullName.getText().trim();
        String contactNumber = txtContactNumber.getText().trim();
        String email = txtEmail.getText().trim();
        String ageText = txtAge.getText().trim();
        String gender = cmbGender.getValue();
        String address = txtAddress.getText().trim();
        String guestsText = txtGuests.getText().trim();
        String selectedRoomType = cmbRoomType.getValue();
        Integer roomId = roomTypeMap.get(selectedRoomType);
        LocalDate checkinDate = dpCheckin.getValue();
        LocalDate checkoutDate = dpCheckout.getValue();
        if (!validateInput(fullName, contactNumber, email, ageText, gender, address, guestsText, checkinDate, checkoutDate, roomId)) {
            return;
        }
        int age = Integer.parseInt(ageText);
        int guests = Integer.parseInt(guestsText);
        OndeskBooking booking = new OndeskBooking(fullName, email, contactNumber, age, gender, address, guests, roomId, checkinDate, checkoutDate);
        if (OndeskBookingDAO.saveBooking(booking)) {
            CustomAlert.show( "Success", "Booking saved successfully!");
            clearForm();
        } else {
            CustomAlert.show( "Error", "Booking could not be saved.");
        }
    }

    private boolean validateInput(String fullName, String contactNumber, String email, String ageText, String gender, String address, String guestsText, LocalDate checkinDate, LocalDate checkoutDate, Integer roomId) {
        if (fullName.isEmpty() || contactNumber.isEmpty() || ageText.isEmpty() || guestsText.isEmpty() || address.isEmpty() || checkinDate == null || checkoutDate == null || gender == null) {
            CustomAlert.show( "Validation Error", "All fields are required!");
            return false;
        }
        if (roomId == null) {
            CustomAlert.show( "Validation Error", "No room selected!");
            return false;
        }
        if (!contactNumber.matches("\\d{10}")) {
            CustomAlert.show( "Validation Error", "Enter a valid 10-digit contact number.");
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            CustomAlert.show( "Validation Error", "Enter a valid email address.");
            return false;
        }
        try {
            int age = Integer.parseInt(ageText);
            int guests = Integer.parseInt(guestsText);
            if (age < 0 || age > 120 || guests < 1) {
                CustomAlert.show( "Validation Error", "Enter valid age and guests.");
                return false;
            }
        } catch (NumberFormatException e) {
            CustomAlert.show( "Validation Error", "Age and Guests must be numbers.");
            return false;
        }
        if (checkoutDate.isBefore(checkinDate)) {
            CustomAlert.show( "Validation Error", "Checkout must be after check-in.");
            return false;
        }
        return true;
    }

    @FXML
    private void clearForm() {
        txtFullName.clear();
        txtContactNumber.clear();
        txtEmail.clear();
        txtAge.clear();
        txtAddress.clear();
        txtGuests.clear();
        cmbGender.setValue(null);
        cmbRoomType.setValue(null);
        dpCheckin.setValue(null);
        dpCheckout.setValue(null);
    }


}

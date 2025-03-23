package com.lordscave.hotel_lord.Controllers;

import com.lordscave.hotel_lord.Entities.BookingRequest;
import com.lordscave.hotel_lord.utilities.DarkThemeAlert;
import com.lordscave.hotel_lord.utilities.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OnlineBookingRequestsController {

    @FXML private TableView<BookingRequest> tblBookings;
    @FXML private TableColumn<BookingRequest, String> colbookingNo, colbookingType, colGuestName, colContactNumber, colCheckinDate, colCheckoutDate, colBookingStatus, colroomType;
    @FXML private Button closeBtn;
    @FXML private HBox titleBar;

    private double xOffset = 0, yOffset = 0;
    private ObservableList<BookingRequest> bookingRequests = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (closeBtn != null) closeBtn.setOnAction(event -> closeWindow());
        setupDraggableWindow();
        setupTable();
        loadBookingRequests();
    }

    private void setupTable() {
        colbookingNo.setCellValueFactory(new PropertyValueFactory<>("bookingNo"));
        colbookingType.setCellValueFactory(new PropertyValueFactory<>("bookingType"));
        colGuestName.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        colContactNumber.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colCheckinDate.setCellValueFactory(new PropertyValueFactory<>("checkinDate"));
        colCheckoutDate.setCellValueFactory(new PropertyValueFactory<>("checkoutDate"));
        colBookingStatus.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));
        colroomType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        tblBookings.setRowFactory(tv -> {
            TableRow<BookingRequest> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) showApprovalPopup(row.getItem());
            });
            return row;
        });
    }

    private void loadBookingRequests() {
        String sql = """
            SELECT b.id, b.bookingtype AS booking_type, b.bookingno AS booking_no,
                   g.username AS guest_name, g.contactno AS contact_no, TO_CHAR(b.checkin_date, 'dd-Mon-yyyy') AS checkin_date,
                   TO_CHAR(b.checkout_date, 'dd-Mon-yyyy') AS checkout_date,
                   b.payment_status AS booking_status, r.roomtype AS room_type
            FROM hotel_booking_requests b
            INNER JOIN hotel_guests g ON g.id = b.user_id
            INNER JOIN room_types r ON r.id = b.roomtype
            WHERE b.isshowasreq = true AND b.isdeleted = false
            ORDER BY b.bookingno
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            assert conn != null;
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                ObservableList<BookingRequest> newBookingRequests = FXCollections.observableArrayList();
                while (rs.next()) {
                    newBookingRequests.add(new BookingRequest(
                            rs.getInt("id"),
                            rs.getString("booking_no"),
                            rs.getString("booking_type"),
                            rs.getString("guest_name"),
                            rs.getString("contact_no"),
                            rs.getString("checkin_date"),
                            rs.getString("checkout_date"),
                            rs.getString("booking_status"),
                            rs.getString("room_type")
                    ));
                }
                Platform.runLater(() -> {
                    bookingRequests.setAll(newBookingRequests);
                    tblBookings.setItems(bookingRequests);
                    tblBookings.refresh();
                });
            }
        } catch (SQLException e) {
            System.err.println("Error loading booking requests: " + e.getMessage());
        }
    }

    private void setupDraggableWindow() {
        if (titleBar == null) return;
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

    private void showApprovalPopup(BookingRequest booking) {
        DarkThemeAlert.show(
                "Booking Approval",
                "Booking No: " + booking.getBookingNo() + "\nApprove or Reject this booking?",
                () -> approveBooking(booking),
                () -> rejectBooking(booking)
        );
    }

    private void approveBooking(BookingRequest booking) {
        String sql = """
            SELECT g.username, g.contactno, g.address, g.age, g.gender, g.email,b.checkin_date, b.checkout_date, x.roomtype, b.roomtype AS roomtypeid, x.ratepernight
            FROM hotel_booking_requests b
            INNER JOIN hotel_guests g ON b.user_id = g.id
            LEFT JOIN (SELECT id, ratepernight, CONCAT(roomtype, ' | ₹', ratepernight, ' | Beds: ', beds) AS roomtype FROM room_types) x ON x.id = b.roomtype
            WHERE b.id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection()) {
            assert conn != null;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, booking.getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    BookingController controller = BookingController.getInstance();
                    if (controller != null) {
                        controller.loadApprovedBooking(
                                rs.getString("username"), rs.getString("email"), rs.getString("contactno"),
                                rs.getString("address"), rs.getInt("age"), rs.getString("gender"),
                                rs.getDate("checkin_date").toLocalDate(), rs.getDate("checkout_date").toLocalDate(),
                                rs.getString("roomtype"), rs.getInt("roomtypeid"), rs.getInt("ratepernight")
                        );
                    } else {
                        System.err.println("Error: BookingController instance is NULL!");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booking details: " + e.getMessage());
        }
        closeWindow();
    }

    private void rejectBooking(BookingRequest booking) {
        String sql = "UPDATE hotel_booking_requests SET isdeleted = true WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            assert conn != null;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, booking.getId());
                if (stmt.executeUpdate() > 0) {
                    System.out.println("Booking " + booking.getBookingNo() + " marked as deleted.");
                    loadBookingRequests();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error rejecting booking: " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        if (stage != null) stage.close();
    }
}

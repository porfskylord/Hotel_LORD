package com.lordscave.hotel_lord.utilities;

import com.lordscave.hotel_lord.Entities.OndeskBooking;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import java.math.BigDecimal;
import java.sql.*;
import java.util.logging.Level;

public class OndeskBookingDAO {

    public static boolean saveBooking(OndeskBooking booking) {
        String guestInsertSQL = """
                INSERT INTO hotel_guests (username, email, contactno, address, age, gender, password)
                VALUES (?, ?, ?, ?, ?, ?, 'OnDesk')
                ON CONFLICT (contactno) DO UPDATE SET
                username = EXCLUDED.username, email = EXCLUDED.email,
                address = EXCLUDED.address, age = EXCLUDED.age, gender = EXCLUDED.gender
                RETURNING id;
                """;

        String bookingInsertSQL = """
                INSERT INTO hotel_booking_requests (user_id, checkin_date, checkout_date, total_price,
                payment_status, roomtype, noofguest, created_at, updated_at, bookingtype, isshowasreq)
                VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), 'OnDesk', ?);
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                LoggerUtil.log(Level.SEVERE, "Database connection failed.");
                return false;
            }

            conn.setAutoCommit(false);
            int userId;

            try (PreparedStatement guestStmt = conn.prepareStatement(guestInsertSQL)) {
                guestStmt.setString(1, booking.getFullName());
                guestStmt.setString(2, booking.getEmail());
                guestStmt.setString(3, booking.getContactNumber());
                guestStmt.setString(4, booking.getAddress());
                guestStmt.setInt(5, booking.getAge());
                guestStmt.setString(6, booking.getGender());
                ResultSet rs = guestStmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("id");
                } else {
                    throw new SQLException("Failed to retrieve guest ID.");
                }
            }

            try (PreparedStatement bookingStmt = conn.prepareStatement(bookingInsertSQL)) {
                bookingStmt.setInt(1, userId);
                bookingStmt.setDate(2, java.sql.Date.valueOf(booking.getCheckinDate()));
                bookingStmt.setDate(3, java.sql.Date.valueOf(booking.getCheckoutDate()));
                bookingStmt.setBigDecimal(4, BigDecimal.ZERO);
                bookingStmt.setString(5, booking.getPaymentStatus());
                bookingStmt.setInt(6, booking.getRoomId());
                bookingStmt.setInt(7, booking.getGuests());
                bookingStmt.setBoolean(8, true);
                if (bookingStmt.executeUpdate() > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                }
            }
        } catch (SQLException e) {
            LoggerUtil.log(Level.SEVERE, "Error saving on-desk booking: " + e.getMessage());
        }
        return false;
    }

    public static ObservableList<Pair<Integer, String>> loadAvailableRooms() {
        ObservableList<Pair<Integer, String>> availableRooms = FXCollections.observableArrayList();

        String sql = """
                SELECT roomd.id, CONCAT(roomd.roomtype, ' | Available: ', x."No Of Room Available",
                ' | ₹', roomd.ratepernight, ' | Beds: ', roomd.beds) AS roomtype
                FROM room_types roomd
                INNER JOIN (
                    SELECT rt.id, COUNT(*) - COALESCE(req.reqbooking, 0) AS "No Of Room Available"
                    FROM room_types rt
                    INNER JOIN hotel_rooms rm ON rm.roomtypeid = rt.id
                    LEFT JOIN (
                        SELECT roomtype, COUNT(*) AS reqbooking
                        FROM hotel_booking_requests
                        GROUP BY roomtype
                    ) req ON req.roomtype = rt.id
                    WHERE rm.isbooked = FALSE
                    GROUP BY rt.id, req.reqbooking
                ) x ON roomd.id = x.id
                WHERE x."No Of Room Available" > 0
                ORDER BY roomd.ratepernight;
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                availableRooms.add(new Pair<>(rs.getInt("id"), rs.getString("roomtype")));
            }
        } catch (SQLException e) {
            LoggerUtil.log(Level.SEVERE, "Error fetching available rooms: " + e.getMessage());
        }
        return availableRooms;
    }
}

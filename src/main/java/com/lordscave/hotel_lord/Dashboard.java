package com.lordscave.hotel_lord;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Dashboard {

    public VBox mainContent;
    public Label lblTotalBookings;
    public PieChart pieRoomOccupancy;
    public ListView lstNotifications;
    public BarChart barRevenueStats;

    @FXML
    public void initialize() {

        getBookingCount();



        // Revenue Bar Chart
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.getData().add(new XYChart.Data<>("Jan", 400000));
        revenueSeries.getData().add(new XYChart.Data<>("Feb", 500000));
        revenueSeries.getData().add(new XYChart.Data<>("Mar", 450000));
        barRevenueStats.getData().add(revenueSeries);

        // Sample Notifications
        lstNotifications.getItems().addAll(
                "New Booking: Room 205",
                "Room 302 needs cleaning",
                "Pending Bill: Guest #34"
        );
    }

    private void getBookingCount() {
        String sql = "SELECT x.booked_rooms || '/' || x.total_rooms AS \"Total Room Booked\",x.booked_rooms,x.total_rooms " +
                "FROM (SELECT COUNT(CASE WHEN isbooked = TRUE THEN 1 END) AS booked_rooms, " +
                "COUNT(*) AS total_rooms FROM comn_roommaster) AS x";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {  // Use try-with-resources for ResultSet

            if (rs.next()) {  // Check if a result exists
                String bookingInfo = rs.getString("Total Room Booked"); // Retrieve the column value
                lblTotalBookings.setText(bookingInfo); // Set it to the label or text field
                pieRoomOccupancy.getData().addAll(
                        new PieChart.Data("Occupied", rs.getDouble("booked_rooms")),
                        new PieChart.Data("Available", rs.getDouble("total_rooms"))
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

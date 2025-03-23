package com.lordscave.hotel_lord.Controllers;

import com.lordscave.hotel_lord.utilities.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardController {

    @FXML private VBox mainContent;
    @FXML private Label lblTotalBookings;
    @FXML private PieChart pieRoomOccupancy;
    @FXML private ListView<String> lstNotifications;
    @FXML private BarChart<String, Number> barRevenueStats;

    @FXML
    public void initialize() {
        loadBookingCount();
        loadRevenueStats();
        loadNotifications();
    }

    private void loadBookingCount() {
        String sql = "SELECT x.booked_rooms || '/' || x.total_rooms AS \"Total Room Booked\", x.booked_rooms, x.total_rooms " +
                "FROM (SELECT COUNT(CASE WHEN isbooked = TRUE THEN 1 END) AS booked_rooms, " +
                "COUNT(*) AS total_rooms FROM hotel_rooms) AS x";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                lblTotalBookings.setText(rs.getString("Total Room Booked"));
                pieRoomOccupancy.getData().addAll(
                        new PieChart.Data("Occupied", rs.getDouble("booked_rooms")),
                        new PieChart.Data("Available", rs.getDouble("total_rooms") - rs.getDouble("booked_rooms"))
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRevenueStats() {
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.getData().add(new XYChart.Data<>("Jan", 400000));
        revenueSeries.getData().add(new XYChart.Data<>("Feb", 500000));
        revenueSeries.getData().add(new XYChart.Data<>("Mar", 450000));
        barRevenueStats.getData().add(revenueSeries);
    }

    private void loadNotifications() {
        lstNotifications.getItems().addAll(
                "New Booking: Room 205",
                "Room 302 needs cleaning",
                "Pending Bill: Guest #34"
        );
    }
}

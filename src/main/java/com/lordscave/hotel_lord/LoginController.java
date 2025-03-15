package com.lordscave.hotel_lord;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class LoginController {
    @FXML public HBox titleBar;
    public ImageView appIcon;
    public TextField passFieldL;
    public TextField userFieldL;
    private double xOffset = 0;
    private double yOffset = 0;
    private static Stage stage;


    @FXML
    public void initialize() {

        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/lordscave/hotel_lord/images/hotel_lord_icon.png")));
            appIcon.setImage(icon);
        } catch (NullPointerException e) {
            System.err.println("Warning: Application icon not found!");
        }

        // Enable dragging functionality
        titleBar.setOnMousePressed(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

    }

    @FXML
    public void closeWindow() {
        // Closes the application safely
        Platform.exit();
        System.exit(0);
    }

    public void logingIn(ActionEvent actionEvent) {
        try {
            LoaderPopup loader = new LoaderPopup();
            loader.show(); // Show loading screen

            // Run authentication in a background thread
            new Thread(() -> {
                boolean authenticated = false;
                try {
                    Thread.sleep(1000); // Simulate loading delay
                    authenticated = authenticateUser(userFieldL.getText(), passFieldL.getText());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                boolean finalAuthenticated = authenticated;
                Platform.runLater(() -> {
                    loader.close(); // Close loader after authentication is complete

                    if (finalAuthenticated) {
                        System.out.println("Logged in");

                        try {
                            // Load the new scene (main_window.fxml)
                            FXMLLoader loaderscene = new FXMLLoader(getClass().getResource("main_window.fxml"));
                            Parent root = loaderscene.load();

                            // Get the current stage (login window) and close it
                            Stage loginStage = (Stage) userFieldL.getScene().getWindow();
                            loginStage.close();

                            // Open new stage for main window
                            Stage mainStage = new Stage();
                            Scene scene = new Scene(root, 1300, 720); // Define the scene
                            scene.setFill(Color.TRANSPARENT); // Ensures transparency

                            // Apply transparent window style
                            mainStage.initStyle(StageStyle.TRANSPARENT);
                            mainStage.setScene(scene);
                            mainStage.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    } else {
                        System.out.println("Invalid credentials!");
                    }
                });
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean authenticateUser(String username, String password) {
        String sql = "SELECT password FROM sec_users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");

                // Compare user input with hashed password
                return PasswordUtil.verifyPassword(password, storedHashedPassword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false; // Authentication failed
    }

    @FXML
    public void forgotPass() {
        ForgotPasswordService.processForgotPassword(userFieldL.getText());
    }
}

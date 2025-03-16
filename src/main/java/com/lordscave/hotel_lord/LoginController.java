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
import java.util.logging.Level;

public class LoginController {
    @FXML public HBox titleBar;
    @FXML public ImageView appIcon;
    @FXML public TextField passFieldL;
    @FXML public TextField userFieldL;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        loadAppIcon();
        setupDraggableWindow();
    }


    private void loadAppIcon() {
        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                    "/com/lordscave/hotel_lord/images/hotel_lord_icon.png"
            )));
            appIcon.setImage(icon);
        } catch (NullPointerException | IllegalArgumentException e) {
            LoggerUtil.log(Level.WARNING, "Application icon not found! Please check the file path.");
            Platform.runLater(() -> CustomAlert.show("Error", "Something went wrong. Please contact support."));
        }
    }


    private void setupDraggableWindow() {
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
        Platform.exit();
        System.exit(0);
    }

    @FXML
    public void logingIn(ActionEvent actionEvent) {
        LoaderPopup loader = new LoaderPopup();
        loader.show();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                boolean authenticated = authenticateUser(userFieldL.getText(), passFieldL.getText());

                Platform.runLater(() -> {
                    loader.close();
                    handleLoginResult(authenticated);
                });

            } catch (InterruptedException e) {
                LoggerUtil.log(Level.WARNING, "Login thread was interrupted");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LoggerUtil.log(Level.SEVERE, "Unexpected error during authentication");
                Platform.runLater(() -> {
                    loader.close();
                    CustomAlert.show("Error", "An unexpected error occurred. Please try again.");
                });
            }
        }).start();
    }


    private void handleLoginResult(boolean authenticated) {
        if (authenticated) {
            LoggerUtil.log(Level.INFO, "User logged in successfully");
            loadMainWindow();
        } else {
            LoggerUtil.log(Level.WARNING, "Invalid credentials!");
            CustomAlert.show("Login Failed", "Invalid username or password. Please try again.");
        }
    }


    private void loadMainWindow() {
        try {
            FXMLLoader loaderscene = new FXMLLoader(getClass().getResource("main_window.fxml"));
            Parent root = loaderscene.load();

            Stage loginStage = (Stage) userFieldL.getScene().getWindow();
            loginStage.close();

            Stage mainStage = new Stage();
            Scene scene = new Scene(root, 1300, 720);
            scene.setFill(Color.TRANSPARENT);

            mainStage.initStyle(StageStyle.TRANSPARENT);
            mainStage.setScene(scene);
            mainStage.show();

        } catch (Exception e) {
            LoggerUtil.log(Level.SEVERE, "Error loading main window");
            CustomAlert.show("Error", "Failed to load the main window. Please contact support.");
        }
    }


    private boolean authenticateUser(String username, String password) {
        String sql = "SELECT password FROM hotel_staff WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                boolean isVerified = PasswordUtil.verifyPassword(password, storedHashedPassword);

                if (isVerified) {
                    LoggerUtil.log(Level.INFO, "User '" + username + "' successfully authenticated.");
                } else {
                    LoggerUtil.log(Level.WARNING, "Authentication failed for user '" + username + "'. Incorrect password.");
                }
                return isVerified;
            } else {
                LoggerUtil.log(Level.WARNING, "Authentication failed for user '" + username + "'. User not found.");
            }
        } catch (Exception e) {
            LoggerUtil.log(Level.SEVERE, "Error occurred during authentication for user '" + username + "'");
        }

        return false;
    }

    @FXML
    public void forgotPass() {
        ForgotPasswordService.processForgotPassword(userFieldL.getText());
    }
}

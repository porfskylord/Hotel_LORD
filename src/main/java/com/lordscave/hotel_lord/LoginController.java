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

    public void logingIn(ActionEvent actionEvent) {
        try {
            LoaderPopup loader = new LoaderPopup();
            loader.show();


            new Thread(() -> {
                boolean authenticated = false;
                try {
                    Thread.sleep(1000);
                    authenticated = authenticateUser(userFieldL.getText(), passFieldL.getText());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                boolean finalAuthenticated = authenticated;
                Platform.runLater(() -> {
                    loader.close();

                    if (finalAuthenticated) {
                        System.out.println("Logged in");

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


                return PasswordUtil.verifyPassword(password, storedHashedPassword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @FXML
    public void forgotPass() {
        ForgotPasswordService.processForgotPassword(userFieldL.getText());
    }
}

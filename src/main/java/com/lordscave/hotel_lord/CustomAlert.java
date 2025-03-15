package com.lordscave.hotel_lord;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class CustomAlert {

    @FXML private VBox root;
    @FXML private HBox titleBar;
    @FXML private Label alertTitle;
    @FXML private Label alertMessage;
    @FXML private Button closeBtn;
    @FXML private Button okBtn;
    @FXML private ImageView appIcon;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {

        closeBtn.setOnAction(event -> ((Stage) closeBtn.getScene().getWindow()).close());
        okBtn.setOnAction(event -> ((Stage) okBtn.getScene().getWindow()).close());


        if (appIcon != null) {
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/lordscave/hotel_lord/images/hotel_lord_icon.png")));
                appIcon.setImage(icon);
            } catch (NullPointerException e) {
                System.err.println("Warning: Application icon not found!");
            }
        }


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

    private static void showAlert(String title, String message, boolean waitForResponse) {
        try {
            FXMLLoader loader = new FXMLLoader(CustomAlert.class.getResource("CustomAlert.fxml"));
            Parent root = loader.load();

            CustomAlert controller = loader.getController();
            controller.alertTitle.setText(title);
            controller.alertMessage.setText(message);

            Stage alertStage = new Stage();
            alertStage.initModality(Modality.APPLICATION_MODAL);
            alertStage.initStyle(StageStyle.UNDECORATED);
            alertStage.setScene(new Scene(root, 530, 180));

            if (waitForResponse) {
                alertStage.showAndWait();
            } else {
                alertStage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void show(String title, String message) {
        showAlert(title, message, false);
    }

    public static void showAndWait(String title, String message) {
        showAlert(title, message, true);
    }
}

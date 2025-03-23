package com.lordscave.hotel_lord.utilities;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class CustomNotification {
    private static final List<Stage> activeNotifications = new ArrayList<>();

    public static void show(String title, String message) {
        Platform.runLater(() -> {
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label messageLabel = new Label(message);
            messageLabel.setStyle("-fx-text-fill: lightgray; -fx-font-size: 13px;");

            VBox notificationContent = new VBox(5, titleLabel, messageLabel);
            notificationContent.setPadding(new Insets(15));
            notificationContent.setAlignment(Pos.CENTER_LEFT);

            StackPane notificationBox = new StackPane(notificationContent);
            notificationBox.setStyle("-fx-background-color: rgba(30, 30, 30, 0.95); -fx-border-radius: 10px; -fx-background-radius: 10px;");
            notificationBox.setPrefWidth(320);
            notificationBox.setEffect(new DropShadow(10, Color.BLACK));

            Stage notificationStage = new Stage();
            notificationStage.initStyle(StageStyle.TRANSPARENT);
            notificationStage.setAlwaysOnTop(true);

            Scene scene = new Scene(notificationBox);
            scene.setFill(Color.TRANSPARENT);
            notificationStage.setScene(scene);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double basePosX = screenBounds.getWidth() - 340;
            double basePosY = screenBounds.getHeight() - 80;
            double offset = activeNotifications.size() * 80;

            notificationStage.setX(basePosX);
            notificationStage.setY(basePosY - offset);

            activeNotifications.add(notificationStage);
            notificationStage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notificationBox);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.seconds(4));
            fadeOut.setOnFinished(e -> {
                notificationStage.close();
                activeNotifications.remove(notificationStage);
            });
            fadeOut.play();
        });
    }
}

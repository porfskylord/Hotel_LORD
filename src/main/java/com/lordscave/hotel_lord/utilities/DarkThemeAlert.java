package com.lordscave.hotel_lord.utilities;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DarkThemeAlert {

    public static void show(String title, String message, Runnable onApprove, Runnable onReject) {
        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL);
        alertStage.initStyle(StageStyle.UNDECORATED);

        VBox sidebar = new VBox();
        sidebar.setPrefWidth(10);
        sidebar.setStyle("-fx-background-color: #ff4757; -fx-border-radius: 10px 0 0 10px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ccc;");
        messageLabel.setWrapText(true);

        VBox textContainer = new VBox(10, titleLabel, messageLabel);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        textContainer.setPadding(new Insets(15, 20, 15, 20));

        Button approveButton = createButton("Approve", "#2ecc71", () -> {
            alertStage.close();
            if (onApprove != null) onApprove.run();
        });

        Button rejectButton = createButton("Reject", "#e74c3c", () -> {
            alertStage.close();
            if (onReject != null) onReject.run();
        });

        HBox buttonContainer = new HBox(15, approveButton, rejectButton);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(10, 0, 15, 0));

        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(sidebar);
        mainLayout.setCenter(new VBox(textContainer, buttonContainer));
        mainLayout.setStyle("-fx-background-color: #222; -fx-border-radius: 10px; -fx-border-color: #444;");
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout, 400, 200);
        alertStage.setScene(scene);
        alertStage.showAndWait();
    }

    private static Button createButton(String text, String color, Runnable action) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px;");
        button.setPrefSize(120, 30);
        button.setOnAction(e -> action.run());
        return button;
    }
}

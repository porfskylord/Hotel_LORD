package com.lordscave.hotel_lord;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HotelLord extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        LoggerUtil.setDatabaseLoggingEnabled(false);

        FXMLLoader fxmlLoader = new FXMLLoader(HotelLord.class.getResource("Login.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 565, 280);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.show();

        LoggerUtil.log(java.util.logging.Level.INFO, "Application started successfully.");
    }

    public static void main(String[] args) {
        launch();
    }
}

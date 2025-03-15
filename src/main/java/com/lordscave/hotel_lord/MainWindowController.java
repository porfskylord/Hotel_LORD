package com.lordscave.hotel_lord;

import javafx.application.Platform;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainWindowController {
    public Button toggleButton;
    public BorderPane mainBorderPane;
    @FXML private VBox menuContainer;

    private boolean sidebarVisible = true;


    private Map<String, String> menuMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadMenuItems();
        loadPage("dashboard.fxml");
    }

    @FXML
    private void toggleSidebar() {
        if (mainBorderPane == null) {
            System.out.println("Error: BorderPane reference is null!");
            return;
        }

        if (sidebarVisible) {

            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), menuContainer);
            slideOut.setToX(-menuContainer.getWidth());
            slideOut.setOnFinished(event -> mainBorderPane.setLeft(null));
            slideOut.play();
        } else {

            mainBorderPane.setLeft(menuContainer);
            menuContainer.setTranslateX(-menuContainer.getWidth());

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), menuContainer);
            slideIn.setToX(0);
            slideIn.play();
        }

        sidebarVisible = !sidebarVisible;
    }

    @FXML
    public void closeWindow() {

        Platform.exit();
        System.exit(0);
    }

    private void loadMenuItems() {
        List<MenuItem> menuItems = DatabaseConnection.getMenuItems();

        for (MenuItem item : menuItems) {
            Hyperlink link = new Hyperlink(item.getName());
            link.setOnAction(e -> loadPage(item.getFxmlPath()));
            link.setStyle("-fx-text-fill: white; -fx-underline: false; "
                    + "-fx-border-color: transparent; -fx-background-color: transparent;");
            menuContainer.getChildren().add(link);
        }
    }

    private void loadPage(String fxmlFile) {
        try {
            Node page = FXMLLoader.load(getClass().getResource(fxmlFile));
            mainBorderPane.setCenter(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

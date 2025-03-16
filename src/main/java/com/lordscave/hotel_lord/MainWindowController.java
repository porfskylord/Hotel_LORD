package com.lordscave.hotel_lord;

import javafx.application.Platform;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MainWindowController {
    public Button toggleButton;
    public BorderPane mainBorderPane;
    @FXML private VBox menuContainer;

    private boolean sidebarVisible = true;
    private Map<String, String> menuMap = new HashMap<>();

    @FXML
    public void initialize() {
        LoggerUtil.log(Level.INFO, "Initializing MainWindowController...");
        loadMenuItems();
        loadPage("dashboard.fxml");
    }

    @FXML
    private void toggleSidebar() {
        if (mainBorderPane == null) {
            LoggerUtil.log(Level.SEVERE, "Error: BorderPane reference is null!");
            return;
        }

        LoggerUtil.log(Level.INFO, "Toggling sidebar: " + (sidebarVisible ? "Hiding" : "Showing"));

        if (sidebarVisible) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), menuContainer);
            slideOut.setToX(-menuContainer.getWidth());
            slideOut.setOnFinished(event -> {
                mainBorderPane.setLeft(null);
                LoggerUtil.log(Level.INFO, "Sidebar hidden successfully.");
            });
            slideOut.play();
        } else {
            mainBorderPane.setLeft(menuContainer);
            menuContainer.setTranslateX(-menuContainer.getWidth());

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), menuContainer);
            slideIn.setToX(0);
            slideIn.play();
            LoggerUtil.log(Level.INFO, "Sidebar displayed successfully.");
        }

        sidebarVisible = !sidebarVisible;
    }

    @FXML
    public void closeWindow() {
        LoggerUtil.log(Level.INFO, "Application is closing.");
        Platform.exit();
        System.exit(0);
    }

    private void loadMenuItems() {
        LoggerUtil.log(Level.INFO, "Loading menu items...");

        List<MenuItem> menuItems = DatabaseConnection.getMenuItems();
        if (menuItems.isEmpty()) {
            LoggerUtil.log(Level.WARNING, "No menu items found in database.");
        }

        for (MenuItem item : menuItems) {
            Hyperlink link = new Hyperlink(item.getName());
            link.setOnAction(e -> loadPage(item.getFxmlPath()));
            link.setStyle("-fx-text-fill: white; -fx-underline: false; "
                    + "-fx-border-color: transparent; -fx-background-color: transparent;");
            menuContainer.getChildren().add(link);
            LoggerUtil.log(Level.INFO, "Added menu item: " + item.getName());
        }
    }

    private void loadPage(String fxmlFile) {
        try {
            LoggerUtil.log(Level.INFO, "Loading page: " + fxmlFile);
            Node page = FXMLLoader.load(getClass().getResource(fxmlFile));
            mainBorderPane.setCenter(page);
        } catch (Exception e) {
            LoggerUtil.log(Level.SEVERE, "Failed to load page: " + fxmlFile, e);
        }
    }
}

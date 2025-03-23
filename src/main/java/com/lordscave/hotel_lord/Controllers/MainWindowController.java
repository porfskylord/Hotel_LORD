package com.lordscave.hotel_lord.Controllers;

import com.lordscave.hotel_lord.utilities.CustomNotification;
import com.lordscave.hotel_lord.utilities.DatabaseConnection;
import com.lordscave.hotel_lord.Entities.MenuItem;
import com.lordscave.hotel_lord.utilities.LoggerUtil;
import javafx.application.Platform;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class MainWindowController {

    @FXML private VBox menuContainer;
    @FXML private BorderPane mainBorderPane;
    @FXML private Button toggleButton;

    private boolean sidebarVisible = true;
    private final Map<String, String> menuMap = new HashMap<>();

    @FXML
    public void initialize() {
        LoggerUtil.log(Level.INFO, "Initializing MainWindowController...");
        loadMenuItems();
        loadPage("/com/lordscave/hotel_lord/Dashboard.fxml");
        Platform.runLater(() ->
                CustomNotification.show("Welcome!", "🏨 Welcome to HotelLord Management System!")
        );
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
            LoggerUtil.log(Level.WARNING, "No menu items found in the database.");
        }
        for (MenuItem item : menuItems) {
            try {
                InputStream iconStream = getClass().getResourceAsStream(item.getIconPath());
                if (iconStream == null) {
                    throw new NullPointerException("Icon not found: " + item.getIconPath());
                }
                ImageView icon = new ImageView(new Image(iconStream));
                icon.setFitWidth(20);
                icon.setFitHeight(20);
                icon.setOnMouseClicked(e -> loadPage(item.getFxmlPath()));
                Hyperlink link = new Hyperlink(item.getName());
                link.setOnAction(e -> loadPage(item.getFxmlPath()));
                link.setStyle("-fx-text-fill: white; -fx-underline: false;-fx-border-color: transparent; -fx-background-color: transparent;");
                HBox menuItemBox = new HBox(5, icon, link);
                menuItemBox.setAlignment(Pos.CENTER_LEFT);
                menuContainer.getChildren().add(menuItemBox);
                LoggerUtil.log(Level.INFO, "Added menu item: " + item.getName());
            } catch (Exception e) {
                LoggerUtil.log(Level.SEVERE, "Failed to load menu item: " + item.getName(), e);
            }
        }
    }

    private void loadPage(String fxmlFile) {
        try {
            LoggerUtil.log(Level.INFO, "Loading page: " + fxmlFile);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent page = loader.load();
            String css = Objects.requireNonNull(getClass().getResource("/com/lordscave/hotel_lord/static/styles.css")).toExternalForm();
            page.getStylesheets().add(css);
            mainBorderPane.setCenter(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

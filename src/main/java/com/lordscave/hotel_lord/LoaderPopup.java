package com.lordscave.hotel_lord;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class LoaderPopup {
    private final Stage stage;

    public LoaderPopup() {
        stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        // Create star lines
        List<Line> starLines = createStarLines();
        Pane root = new Pane();
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");

        // Add lines to root but keep them invisible initially
        for (Line line : starLines) {
            line.setStrokeWidth(4);
            line.setStrokeLineCap(StrokeLineCap.ROUND);
            line.setOpacity(0); // Initially hidden
            root.getChildren().add(line);
        }

        // Animate the star being drawn with gradient color
        animateStarDrawing(starLines);

        Scene scene = new Scene(root, 120, 120);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
    }

    // Create a properly connected 5-pointed star using lines
    private List<Line> createStarLines() {
        double centerX = 60, centerY = 60, radius = 40;
        double innerRadius = radius / 2.5; // Smaller inner points
        double angle = Math.toRadians(-90); // Start at top

        List<Line> lines = new ArrayList<>();
        double[][] points = new double[10][2];

        // Calculate outer and inner points
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? radius : innerRadius;
            points[i][0] = centerX + r * Math.cos(angle);
            points[i][1] = centerY + r * Math.sin(angle);
            angle += Math.toRadians(36); // Move 36° each step
        }

        // Connect points in correct star sequence
        int[] order = {0, 5, 2, 7, 4, 9, 6, 1, 8, 3, 0}; // Proper star connection order
        for (int i = 0; i < order.length - 1; i++) {
            Line line = new Line(points[order[i]][0], points[order[i]][1],
                    points[order[i + 1]][0], points[order[i + 1]][1]);
            line.setStroke(createGradient()); // Correctly applies the gradient
            lines.add(line);
        }

        return lines;
    }

    // Animate the star being drawn line by line using FadeTransition
    private void animateStarDrawing(List<Line> starLines) {
        List<FadeTransition> transitions = new ArrayList<>();

        for (Line line : starLines) {
            FadeTransition transition = new FadeTransition(Duration.seconds(0.1), line);
            transition.setFromValue(0);
            transition.setToValue(1);
            transitions.add(transition);
        }

        // Play the fade animations sequentially (one after another)
        SequentialTransition sequentialTransition = new SequentialTransition(transitions.toArray(new FadeTransition[0]));
        sequentialTransition.setCycleCount(SequentialTransition.INDEFINITE);
        sequentialTransition.play();
    }

    // Apply a gradient stroke dynamically for each line
    private LinearGradient createGradient() {
        return new LinearGradient(0, 0, 1, 0, true, null,
                new Stop(0, Color.web("#6A11CB")),
                new Stop(1, Color.web("#2575FC"))
        );
    }

    public void show() {
        Platform.runLater(stage::show);
    }

    public void close() {
        Platform.runLater(stage::close);
    }
}

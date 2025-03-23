package com.lordscave.hotel_lord.utilities;

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

        Pane root = new Pane();
        root.setStyle("-fx-background-color: transparent;");

        List<Line> starLines = createStarLines();
        starLines.forEach(root.getChildren()::add);
        animateStarDrawing(starLines);

        Scene scene = new Scene(root, 120, 120, Color.TRANSPARENT);
        stage.setScene(scene);
    }

    private List<Line> createStarLines() {
        double centerX = 60, centerY = 60, radius = 40;
        double innerRadius = radius / 2.5;
        double angle = Math.toRadians(-90);
        List<Line> lines = new ArrayList<>();
        double[][] points = new double[10][2];

        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? radius : innerRadius;
            points[i][0] = centerX + r * Math.cos(angle);
            points[i][1] = centerY + r * Math.sin(angle);
            angle += Math.toRadians(36);
        }

        int[] order = {0, 5, 2, 7, 4, 9, 6, 1, 8, 3, 0};
        for (int i = 0; i < order.length - 1; i++) {
            Line line = new Line(points[order[i]][0], points[order[i]][1],
                    points[order[i + 1]][0], points[order[i + 1]][1]);
            line.setStroke(createGradient());
            line.setStrokeWidth(4);
            line.setStrokeLineCap(StrokeLineCap.ROUND);
            line.setOpacity(0);
            lines.add(line);
        }
        return lines;
    }

    private void animateStarDrawing(List<Line> starLines) {
        SequentialTransition sequentialTransition = new SequentialTransition();
        for (Line line : starLines) {
            FadeTransition transition = new FadeTransition(Duration.seconds(0.1), line);
            transition.setFromValue(0);
            transition.setToValue(1);
            sequentialTransition.getChildren().add(transition);
        }
        sequentialTransition.setCycleCount(SequentialTransition.INDEFINITE);
        sequentialTransition.play();
    }

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

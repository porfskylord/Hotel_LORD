package com.lordscave.hotel_lord.utilities;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

public class SignatureCapture {

    public static void showSignaturePad(Stage parentStage) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(parentStage);
        stage.setTitle("Capture Signature");

        Canvas canvas = new Canvas(400, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 400, 200);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        canvas.setOnMousePressed(e -> gc.beginPath());
        canvas.setOnMouseDragged(e -> {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        });

        Button saveButton = new Button("Save Signature");
        saveButton.setOnAction(e -> {
            WritableImage image = new WritableImage(400, 200);
            canvas.snapshot(null, image);
            File file = new File("signature.png");
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                stage.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(canvas, saveButton);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}

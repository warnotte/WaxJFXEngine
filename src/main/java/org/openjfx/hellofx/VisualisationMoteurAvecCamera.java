package org.openjfx.hellofx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

public class VisualisationMoteurAvecCamera extends Application {

    private double lastX, lastY; // Pour la translation
    private double startX, startY; // Origine pour le rectangle de sélection

    private double zoomFactor = -1000; // Distance initiale de la caméra

    private final HashMap<Rectangle, Flotteur> rectangleToFlotteurMap = new HashMap<>();
    private final Set<Flotteur> selectedFlotteurs = new HashSet<>();

    private Rectangle selectionRectangle;

    // Calque pour l'espace monde
    Group drawingGroup = new Group();

    // Calque pour les éléments d'interface fixe
    Group overlayGroup = new Group();

    @Override
    public void start(Stage primaryStage) {

        // Ajouter des rectangles dans l'espace monde
        for (int i = 0; i < 10; i++) {
            Rectangle rect = new Rectangle(50 + i * 70, 50, 60, 40);
            rect.setFill(Color.BLUE);
            rect.setStroke(Color.BLACK);
            drawingGroup.getChildren().add(rect);

            Flotteur flotteur = new Flotteur("Flotteur " + i);
            rectangleToFlotteurMap.put(rect, flotteur);
        }

        // Ajouter un label pour afficher les coordonnées de la souris
        Label mouseCoordsLabel = new Label("Coordonnées : (x, y)");
        mouseCoordsLabel.setStyle("-fx-font-size: 14px; -fx-background-color: rgba(255, 255, 255, 0.8); -fx-padding: 5px;");
        mouseCoordsLabel.setLayoutX(10);
        mouseCoordsLabel.setLayoutY(10);

        overlayGroup.getChildren().add(mouseCoordsLabel);

        // Conteneur principal
        Pane worldPane = new Pane(drawingGroup);
        Scene scene = new Scene(new Pane(worldPane, overlayGroup), 800, 600);

        // Caméra pour le monde
        PerspectiveCamera camera = new PerspectiveCamera(false);
        scene.setCamera(camera);
        camera.setTranslateZ(zoomFactor);

        // Gestion du zoom avec la molette
        scene.setOnScroll(event -> {
            double zoomDelta = event.getDeltaY() > 0 ? -50 : 50; // Zoom avant ou arrière
            camera.setTranslateZ(camera.getTranslateZ() + zoomDelta);
        });

        // Gestion de la translation avec le bouton du milieu
        scene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                lastX = event.getSceneX();
                lastY = event.getSceneY();
            }
        });

        scene.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                double deltaX = event.getSceneX() - lastX;
                double deltaY = event.getSceneY() - lastY;
                camera.setTranslateX(camera.getTranslateX() - deltaX);
                camera.setTranslateY(camera.getTranslateY() - deltaY);
                lastX = event.getSceneX();
                lastY = event.getSceneY();
            }
        });

        // Gestion de la sélection
        scene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                startX = event.getX();
                startY = event.getY();

                if (selectionRectangle == null) {
                    selectionRectangle = new Rectangle();
                    selectionRectangle.setFill(Color.color(0, 0, 1, 0.2));
                    selectionRectangle.setStroke(Color.BLUE);
                    overlayGroup.getChildren().add(selectionRectangle);
                }
                selectionRectangle.setX(startX);
                selectionRectangle.setY(startY);
                selectionRectangle.setWidth(0.0);
                selectionRectangle.setHeight(0.0);
            }
        });

        scene.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY && selectionRectangle != null) {
                double currentX = event.getX();
                double currentY = event.getY();

                double x = Math.min(currentX, startX);
                double y = Math.min(currentY, startY);
                double width = Math.abs(currentX - startX);
                double height = Math.abs(currentY - startY);

                selectionRectangle.setX(x);
                selectionRectangle.setY(y);
                selectionRectangle.setWidth(width);
                selectionRectangle.setHeight(height);
            }
        });

        scene.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY && selectionRectangle != null) {
                Rectangle selectionShape = new Rectangle(
                    selectionRectangle.getX(), selectionRectangle.getY(),
                    selectionRectangle.getWidth(), selectionRectangle.getHeight()
                );

                selectedFlotteurs.clear();
                rectangleToFlotteurMap.forEach((shape, flotteur) -> {
                    if (Shape.intersect(selectionShape, shape).getBoundsInLocal().getWidth() > 0) {
                        shape.setFill(Color.RED);
                        selectedFlotteurs.add(flotteur);
                    } else {
                        shape.setFill(Color.BLUE);
                    }
                });

                overlayGroup.getChildren().remove(selectionRectangle);
                selectionRectangle = null;
            }
        });

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                clearSelection();
            }
        });

        primaryStage.setTitle("Moteur de Visualisation avec Camera");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void clearSelection() {
        rectangleToFlotteurMap.forEach((rect, flotteur) -> rect.setFill(Color.BLUE));
        selectedFlotteurs.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }

    static class Flotteur {
        private final String name;

        public Flotteur(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

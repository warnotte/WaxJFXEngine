package org.openjfx.hellofx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TEST extends Application {

    private double lastX, lastY;
    private double startX, startY; // Origine constante pour le rectangle de sélection

    private double zoomFactor = 1.0;

    private final HashMap<Rectangle, Flotteur> rectangleToFlotteurMap = new HashMap<>();
    private final Set<Flotteur> selectedFlotteurs = new HashSet<>();

    private Rectangle selectionRectangle;

    
    // Calque pour l'espace monde (éléments dessinés)
    Group drawingGroup = new Group();

    // Calque pour l'overlay (éléments fixes comme la sélection)
    Group overlayGroup = new Group();

    // Pane racine contenant les deux calques
    Pane drawingLayer = new Pane(drawingGroup); // Contient l'espace monde
    Pane uiLayer = new Pane(overlayGroup); // Contient l'interface utilisateur
    Pane root = new Pane(drawingLayer, uiLayer);

    
    @Override
    public void start(Stage primaryStage) {
    	Pane root = new Pane();
    	Group drawingLayer = new Group();
    	Rectangle rect = new Rectangle(100, 100, 200, 150);
    	rect.setFill(Color.BLUE);
    	drawingLayer.getChildren().add(rect);
    	root.getChildren().add(drawingLayer);

    	Scene scene = new Scene(root, 800, 600);
/*
    	scene.setOnScroll(event -> {
    	    double zoomDelta = event.getDeltaY() > 0 ? 1.1 : 0.9;

    	    zoomFactor *= zoomDelta;
    	    drawingLayer.setScaleX(zoomFactor);
    	    drawingLayer.setScaleY(zoomFactor);

    	    System.out.println("Zoom Factor: " + zoomFactor);
    	});
    	*/
    	scene.setOnScroll(event -> {
    	    // Déterminer le facteur de zoom
    	    double zoomDelta = event.getDeltaY() > 0 ? 1.1 : 0.9;

    	    // Position actuelle de la souris dans la scène
    	    double mouseSceneX = event.getSceneX();
    	    double mouseSceneY = event.getSceneY();

    	    // Convertir la position de la souris dans l'espace local avant le zoom
    	    Point2D mouseLocalBeforeZoom = drawingLayer.sceneToLocal(mouseSceneX, mouseSceneY);

    	    // Appliquer le facteur de zoom
    	    zoomFactor *= zoomDelta;
    	    drawingLayer.setScaleX(zoomFactor);
    	    drawingLayer.setScaleY(zoomFactor);

    	    // Convertir à nouveau la position de la souris dans l'espace local après le zoom
    	    Point2D mouseLocalAfterZoom = drawingLayer.sceneToLocal(mouseSceneX, mouseSceneY);

    	    // Calculer la différence entre les positions locales avant et après le zoom
    	    double deltaX = mouseLocalAfterZoom.getX() - mouseLocalBeforeZoom.getX();
    	    double deltaY = mouseLocalAfterZoom.getY() - mouseLocalBeforeZoom.getY();

    	    // Ajuster les translations pour compenser le mouvement
    	    drawingLayer.setTranslateX(drawingLayer.getTranslateX() + deltaX * zoomFactor);
    	    drawingLayer.setTranslateY(drawingLayer.getTranslateY() + deltaY * zoomFactor);

    	    // Logs pour debug
    	    System.out.println("Zoom Factor: " + zoomFactor);
    	    System.out.println("Mouse Scene Position: (" + mouseSceneX + ", " + mouseSceneY + ")");
    	    System.out.println("TranslateX: " + drawingLayer.getTranslateX() + ", TranslateY: " + drawingLayer.getTranslateY());
    	});

    	primaryStage.setScene(scene);
    	primaryStage.show();

    }

    

    public static void main(String[] args) {
        TEST.launch(args);
    }

}

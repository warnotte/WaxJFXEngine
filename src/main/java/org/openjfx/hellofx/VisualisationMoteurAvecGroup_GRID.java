package org.openjfx.hellofx;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.geometry.BoundingBox;

public class VisualisationMoteurAvecGroup_GRID extends Pane {

    private double lastX, lastY; // Pour la translation
    private double zoomFactor = 1.0; // Facteur de zoom

    private Group drawingGroup = new Group(); // Calque pour les éléments dessinés
    private Pane drawingLayer = new Pane(drawingGroup); // Espace monde

    private Group gridGroup = new Group(); // Groupe pour la grille

    private MouseButton buttonTranslation = MouseButton.MIDDLE; // Bouton pour translation

    public VisualisationMoteurAvecGroup_GRID() {
        super();
        getChildren().addAll(gridGroup, drawingLayer); // Ajouter la grille avant les objets
        createScene();
        createGrid(); // Initialiser la grille
    }

    private void createScene() {
        initializeObjectsToDraw(); // Ajouter des objets de démonstration

        setOnScroll(event -> OnScroll(event)); // Zoom avec la molette
        setOnMousePressed(event -> OnMousePressed(event)); // Début translation
        setOnMouseDragged(event -> OnMouseDragged(event)); // Translation
        setOnMouseMoved(event -> {
            double worldX = drawingLayer.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
            double worldY = drawingLayer.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
            System.err.println("Pos : "+worldX+ ", "+worldY);

        });
        
        // load and apply CSS. 
        Optional.ofNullable(getClass().getResource("/FXWView2D.css")) .map(URL::toExternalForm) .ifPresent(getStylesheets()::add); 

    }
    
    private void createGrid() {
    	gridGroup.setMouseTransparent(true);
        gridGroup.getChildren().clear(); // Nettoyer l'ancienne grille

       /* double baseCellSize = 50; // Taille des cellules en pixels écran
        
        if (zoomFactor<0.2)
        	baseCellSize = 100;
        if (zoomFactor>2)
        	baseCellSize = 10;
       */
        
        // TODO : Si on dezoom fort l'espacement doit augmenter toujour avec un multiple de 50
        // Calculer la taille des cellules en fonction du zoom
        double baseCellSize = 50;
        if (zoomFactor < 0.2) {
            baseCellSize = 200; // Grande taille de cellules pour un zoom très éloigné
        } else if (zoomFactor < 0.5) {
            baseCellSize = 100;
        } else if (zoomFactor < 1.0) {
            baseCellSize = 50;
        } else if (zoomFactor < 2.0) {
            baseCellSize = 25;
        } else {
            baseCellSize = 10; // Petites cellules pour un zoom très proche
        }
        
        
        double scaledCellSize = baseCellSize * zoomFactor; // Taille ajustée dans l'espace monde

        // Obtenir les dimensions de la scène (visible à l'écran)
        Scene scene = getScene();
        if (scene == null) return; // Si la scène n'est pas encore initialisée

        double sceneWidth = scene.getWidth();
        double sceneHeight = scene.getHeight();
        
        // Coins visibles de la scène (en pixels écran)
        Point2D topLeftScreen = new Point2D(0, 0); // Coin supérieur gauche
        Point2D bottomRightScreen = new Point2D(sceneWidth, sceneHeight); // Coin inférieur droit
        // Conversion des coordonnées écran -> monde
        Point2D topLeftWorld = drawingLayer.sceneToLocal(topLeftScreen);
        Point2D bottomRightWorld = drawingLayer.sceneToLocal(bottomRightScreen);

        // Coordonnées min et max dans le monde
        double worldMinX = topLeftWorld.getX();
        double worldMinY = topLeftWorld.getY();
        double worldMaxX = bottomRightWorld.getX();
        double worldMaxY = bottomRightWorld.getY();

        // Affichage pour vérification
        System.out.println("World MinX: " + worldMinX + ", MinY: " + worldMinY);
        System.out.println("World MaxX: " + worldMaxX + ", MaxY: " + worldMaxY);
        System.out.println("Zoom : "+zoomFactor);
        
        Point2D pt;
        Line line;
        
        double offsetBorder = 20;
        
        /**
         * Ce block de code sert juste a debugger pour voire si on dessine pas au dela du necessaire.
         */
        /*double WW = Math.abs(worldMaxX-worldMinX);
        double HH = Math.abs(worldMaxY-worldMinY);
        worldMinX = worldMinX + WW/10;
        worldMaxX = worldMaxX + WW/10;
        worldMinY = worldMinY + HH/10;
        worldMaxY = worldMaxY + HH/10;
        */
        // Ajuster la boucle pour dessiner dans l'espace visible du monde
        for (double x = Math.floor(worldMinX / baseCellSize) * baseCellSize; x <= worldMaxX; x += baseCellSize) {
            pt = drawingLayer.localToScene(new Point2D(x, 0));
            line = new Line(0+pt.getX(), offsetBorder, pt.getX(), sceneHeight-offsetBorder); // Lignes couvrant toute la hauteur
            line.getStyleClass().add("grid_otherAxes");
            //line.setStroke(Color.RED);
            //line.setStrokeWidth(0.5); // Épaisseur constante
            gridGroup.getChildren().add(line);

            // Ajouter un label indiquant la position dans le monde
            Text label = new Text(String.format("%.0f", x)); // Formater la position (par exemple sans décimales)
            label.getStyleClass().add("grid_positionTexts");
            //label.setFill(Color.BLUE);
            //label.setX(pt.getX() + 5); // Position du texte légèrement à côté de la ligne
            label.setX(pt.getX() - label.prefWidth(-1) / 2); // Centrer horizontalement
            label.setY(20); // Position du texte légèrement en haut de l'écran
            gridGroup.getChildren().add(label);
        }

        // Ajuster la boucle pour dessiner dans l'espace visible du monde
        for (double y = Math.floor(worldMinY / baseCellSize) * baseCellSize; y <= worldMaxY; y += baseCellSize) {
            pt = drawingLayer.localToScene(new Point2D(0, y));
            line = new Line(0+offsetBorder, pt.getY(), sceneWidth-offsetBorder, pt.getY()); // Lignes couvrant toute la hauteur
            line.getStyleClass().add("grid_otherAxes");
            //line.setStroke(Color.RED);
            //line.setStrokeWidth(0.5); // Épaisseur constante
            gridGroup.getChildren().add(line);
            
            // Ajouter un label indiquant la position dans le monde
            Text label = new Text(String.format("%.0f", y)); // Formater la position (par exemple sans décimales)
            label.getStyleClass().add("grid_positionTexts");
            //label.setFill(Color.BLUE);
            label.setTextAlignment(TextAlignment.CENTER); // Marche pas 
            label.setX(5); // Position légèrement décalée à gauche
            label.setY(pt.getY() + label.prefHeight(-1) /2); // Centrer verticalement sur la ligne // marche pas trop ...
            label.setY(pt.getY());
            gridGroup.getChildren().add(label);
        }
        
        /*
        .grid_centralAxes {
        	
        }

        .grid_otherAxes {
        	
        }

        .grid_positionTexts {
        	
        }*/
        
        /**
         * Dessine les axes au centre du monde.
         */
        pt = drawingLayer.localToScene(new Point2D(0, 0)); 
        
        line = new Line(pt.getX(), offsetBorder, pt.getX(), sceneHeight-offsetBorder);
        line.getStyleClass().add("grid_centralAxes");
        
      //  line.setStroke(Color.GREEN);
        // line.setStrokeWidth(5.5); // Épaisseur constante
        gridGroup.getChildren().add(line);

        line = new Line(offsetBorder, pt.getY(), sceneWidth-offsetBorder, pt.getY());
        line.getStyleClass().add("grid_centralAxes");
      //  line.setStroke(Color.GREEN);
      //  line.setStrokeWidth(5.5); // Épaisseur constante
        gridGroup.getChildren().add(line);

    }



    /*private void createGrid() {
        gridGroup.getChildren().clear(); // Nettoyer l'ancienne grille

        double cellSize = 50; // Taille des cellules dans l'espace monde

        // Obtenir les limites visibles
        Bounds worldBounds = drawingLayer.localToScene(drawingGroup.getLayoutBounds());
        double minX = worldBounds.getMinX();
        double maxX = worldBounds.getMaxX();
        double minY = worldBounds.getMinY();
        double maxY = worldBounds.getMaxY();

        // Ajuster les limites au zoom
        double adjustedMinX = Math.floor(minX / cellSize) * cellSize;
        double adjustedMaxX = Math.ceil(maxX / cellSize) * cellSize;
        double adjustedMinY = Math.floor(minY / cellSize) * cellSize;
        double adjustedMaxY = Math.ceil(maxY / cellSize) * cellSize;

        // Dessiner les lignes verticales
        for (double x = adjustedMinX; x <= adjustedMaxX; x += cellSize) {
            Line line = new Line(x, adjustedMinY, x, adjustedMaxY);
            line.setStroke(Color.LIGHTGRAY);
            line.setStrokeWidth(0.5 / zoomFactor); // Épaisseur constante
            gridGroup.getChildren().add(line);
        }

        // Dessiner les lignes horizontales
        for (double y = adjustedMinY; y <= adjustedMaxY; y += cellSize) {
            Line line = new Line(adjustedMinX, y, adjustedMaxX, y);
            line.setStroke(Color.LIGHTGRAY);
            line.setStrokeWidth(0.5 / zoomFactor); // Épaisseur constante
            gridGroup.getChildren().add(line);
        }
    }*/
    
    
    

    protected void OnScroll(ScrollEvent event) {
        double zoomDelta = event.getDeltaY() > 0 ? 1.1 : 0.9;

        Point2D mouseLocalBeforeZoom = drawingLayer.sceneToLocal(event.getSceneX(), event.getSceneY());
        zoomFactor *= zoomDelta;
        drawingLayer.setScaleX(zoomFactor);
        drawingLayer.setScaleY(zoomFactor);

        Point2D mouseLocalAfterZoom = drawingLayer.sceneToLocal(event.getSceneX(), event.getSceneY());
        double deltaX = mouseLocalAfterZoom.getX() - mouseLocalBeforeZoom.getX();
        double deltaY = mouseLocalAfterZoom.getY() - mouseLocalBeforeZoom.getY();
        drawingLayer.setTranslateX(drawingLayer.getTranslateX() + deltaX * zoomFactor);
        drawingLayer.setTranslateY(drawingLayer.getTranslateY() + deltaY * zoomFactor);

        createGrid(); // Mettre à jour la grille
    }

    protected void OnMousePressed(MouseEvent event) {
        if (event.getButton() == buttonTranslation) {
            lastX = event.getSceneX();
            lastY = event.getSceneY();
        }
    }

    protected void OnMouseDragged(MouseEvent event) {
        if (event.getButton() == buttonTranslation) {
            double deltaX = event.getSceneX() - lastX;
            double deltaY = event.getSceneY() - lastY;
            drawingLayer.setTranslateX(drawingLayer.getTranslateX() + deltaX);
            drawingLayer.setTranslateY(drawingLayer.getTranslateY() + deltaY);
            lastX = event.getSceneX();
            lastY = event.getSceneY();

            createGrid(); // Mettre à jour la grille
        }
    }

    private void initializeObjectsToDraw() {
        Random rand = new Random();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Rectangle rect = new Rectangle(-25, -25, 50, 50);
                rect.setTranslateX(i * 100);
                rect.setTranslateY(j * 100);
                rect.setFill(Color.BLUE);
                rect.setStroke(Color.BLACK);
                drawingGroup.getChildren().add(rect);
            }
        }
    }
/*public static void main(String[] args) {
        Application.launch(VisualisationApp.class, args);
    }
/*
    public static class VisualisationApp extends Application {
        @Override
        public void start(Stage primaryStage) {
            VisualisationMoteurAvecGroup_GRID pane = new VisualisationMoteurAvecGroup_GRID();
            Scene scene = new Scene(pane, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Moteur d'affichage avec grille dynamique");
            primaryStage.show();
        }
    }*/
}
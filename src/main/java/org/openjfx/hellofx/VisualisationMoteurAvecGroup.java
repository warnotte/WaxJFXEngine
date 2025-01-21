package org.openjfx.hellofx;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
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

public class VisualisationMoteurAvecGroup extends Application {

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

        // Style du fond
        root.setStyle("-fx-background-color: lightgray;");

        Rectangle rect1 = new Rectangle(50, 50, 60, 40);
		rect1.setFill(Color.BLUE);
		rect1.setStroke(Color.BLACK);
		rect1.getStyleClass().add("rectangle"); 
		rect1.setStyle(":hover {"
				+ "    -fx-background-color: #383838;"
				+ "    -fx-scale-y: 1.1;"
				+ "}");
		root.getChildren().add(rect1);
        
        
        Group groupeRectangle = new Group();
        groupeRectangle.setTranslateX(300);
        groupeRectangle.setRotate(45);
     	
     	// Ajouter des rectangles dans l'espace monde
        for (int i = 0; i < 8; i++) {
           for (int j = 0; j < 8; j++) {
        		Group flotteurAll = new Group();
        		
        		flotteurAll.setTranslateX(j*70);
    			flotteurAll.setTranslateY(i*50);
    			flotteurAll.setRotate((j+i)*10);
    			
        		
    			Flotteur flotteur = new Flotteur("Flotteur " + j);
    			Rectangle rect = new Rectangle(-30, -20, 60, 40);
    			rect.setFill(Color.BLUE);
    			rect.setStroke(Color.BLACK);
    			//rect.getStyleClass().add("rectangle"); 
    			/*rect.setStyle(":hover {"
    					+ "    -fx-background-color: #383838;"
    					+ "    -fx-scale-y: 1.1;"
    					+ "}");*/
    			
    			
    			flotteurAll.getChildren().add(rect);
    			
    	//		Group arrow = createArrow(drawingLayer, -30, -22, 30, -22, 3, "L");
    	//		flotteurAll.getChildren().add(arrow);
    		    
    			groupeRectangle.getChildren().add(flotteurAll);
    			
    			// Texte en espace ecran
    	//		addLabelToShape("D1_"+i+""+j, rect, overlayGroup, drawingLayer, -30, -20);
    	//	    addLabelToShape("D2_"+i+""+j, rect, overlayGroup, drawingLayer, 0, 0);
    	//	    addLabelToShape("D3_"+i+""+j, rect, overlayGroup, drawingLayer, 30, 20);
    		    
    		    // Texte dans l'espace monde
    		    Text label = new Text(0,5, "XX");
    		    flotteurAll.getChildren().add(label);
    		    
    		    flotteurAll.getStyleClass().add("rectangle");
    		    
    			rectangleToFlotteurMap.put(rect, flotteur);
    		}
        }
        
        /*
        // TODO : Ce code est foireux, celui du dessus me semble bien.
		// Ajouter différentes formes avec des labels
		Shape[] shapes = new Shape[] {
				new Rectangle(50, 50+100, 100, 50),                // Rectangle
				new Rectangle(-50, -20, 100, 40),                // Rectangle
			    new Circle(200, 200+100, 50),                      // Cercle
		    new Polygon(300, 300+100, 350, 250+100, 400, 300+100),     // Triangle
		};

		for (Shape shape : shapes) {
		    shape.setFill(Color.BLUE);
		    shape.setStroke(Color.BLACK);

		    // Créer un groupe pour la forme et le texte
		    Group group = createShapeWithLabel(shape, drawingLayer, 0.5, 0.0);

		    
		    Group shapeArrow = createArrow(drawingLayer, 0,-22, 50, -22, 10, "50m");
		    
		    group.getChildren().add(shapeArrow);
		    
		    
		    // Appliquer des transformations au groupe
		    group.setRotate(30); // Exemple de rotation
		    group.setScaleX(1.0); // Exemple d'échelle sur X
		    group.setScaleY(1.0); // Exemple d'échelle sur Y

		    // Ajouter le groupe au monde
		    drawingGroup.getChildren().add(group);
		}
		*/

		// groupeRectangle.setRotate(45);
		drawingGroup.getChildren().add(groupeRectangle);           

        // Label pour afficher les coordonnées de la souris
        Label mouseCoordsLabel = new Label("Coordonnées : (x, y)");
        mouseCoordsLabel.setStyle("-fx-font-size: 14px;"
        		+ " -fx-background-color: rgba(255, 255, 255, 0.8); "
        		+ "-fx-padding: 5px; "
        		+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 5);"
        		);
        mouseCoordsLabel.setLayoutX(10);
        mouseCoordsLabel.setLayoutY(10);

        // Ajouter le label à l'UI Layer
        uiLayer.getChildren().add(mouseCoordsLabel);

        Scene scene = new Scene(root, 800, 600);

        
        System.err.println(" >> " + getClass().getResource("/test.css"));
     // load and apply CSS. 
        Optional.ofNullable(getClass().getResource("/test.css")) 
                .map(URL::toExternalForm) 
                .ifPresent(scene.getStylesheets()::add); 
        
        
        // Mise à jour des coordonnées de la souris
        scene.setOnMouseMoved(event -> updateMouseCoords(event, drawingLayer, mouseCoordsLabel));
      
        /*
        scene.setOnScroll(event -> {
            double zoomDelta = event.getDeltaY() > 0 ? 1.1 : 0.9;
            
            // Appliquer le zoom
            zoomFactor *= zoomDelta;
            drawingLayer.setScaleX(zoomFactor);
            drawingLayer.setScaleY(zoomFactor);

            // Log des propriétés
            System.out.println("Zoom Factor: " + zoomFactor);
            System.out.println("TranslateX: " + drawingLayer.getTranslateX() + ", TranslateY: " + drawingLayer.getTranslateY());
            System.out.println("ScaleX: " + drawingLayer.getScaleX() + ", ScaleY: " + drawingLayer.getScaleY());
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
   

        // Gestion de la translation
        scene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                lastX = event.getSceneX();
                lastY = event.getSceneY();
            } else if (event.getButton() == MouseButton.PRIMARY) {
                // Début du rectangle de sélection
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
                selectionRectangle.setWidth(0.000);
                selectionRectangle.setHeight(0.000);
            }
        });

        scene.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                double deltaX = event.getSceneX() - lastX;
                double deltaY = event.getSceneY() - lastY;
                drawingLayer.setTranslateX(drawingLayer.getTranslateX() + deltaX);
                drawingLayer.setTranslateY(drawingLayer.getTranslateY() + deltaY);
                lastX = event.getSceneX();
                lastY = event.getSceneY();
            } else if (event.getButton() == MouseButton.PRIMARY && selectionRectangle != null) {
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
            	
                // Si le rectangle de sélection est trop petit, simuler un clic simple
            	double offsetFake = 0.001;
            	
            	selectionRectangle.setWidth(selectionRectangle.getWidth()+offsetFake);
            	selectionRectangle.setHeight(selectionRectangle.getHeight()+offsetFake);
                            	
                // Convertir les limites du rectangle de sélection en coordonnées de l'espace monde
                double selectionX = selectionRectangle.getX();
                double selectionY = selectionRectangle.getY();
                double selectionWidth = selectionRectangle.getWidth();
                double selectionHeight = selectionRectangle.getHeight();

                // Rectangle de sélection comme un objet Shape
                Rectangle selectionShape = new Rectangle(selectionX, selectionY, selectionWidth, selectionHeight);

                // Sélectionner les objets dans le rectangle
                selectedFlotteurs.clear();
                rectangleToFlotteurMap.forEach((shape, flotteur) -> {
                    // Vérifier l'intersection en tenant compte des transformations
                    if (Shape.intersect(selectionShape, shape).getBoundsInLocal().getWidth() > 0) {
                        shape.setFill(Color.RED);
                        selectedFlotteurs.add(flotteur);
                    } else {
                        shape.setFill(Color.BLUE);
                    }
                });

                // Afficher les objets sélectionnés dans la console
                System.out.println("Flotteurs sélectionnés : " + selectedFlotteurs);

                overlayGroup.getChildren().remove(selectionRectangle);
                selectionRectangle = null;
            }
        });


        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                clearSelection();
                System.out.println("Sélection annulée.");
            }
        });

        primaryStage.setTitle("Moteur de Visualisation avec Groupes et Calques");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

 /*  
    private Group createShapeWithLabel(Shape shape, Pane drawingLayer, double offsetX, double offsetY) {
        // Créer un groupe pour regrouper la forme et le texte
        Group group = new Group();

        // Ajouter la forme au groupe
        group.getChildren().add(shape);
        
        

        // Créer le texte
        Text label = new Text("Dimension");
        label.setFill(Color.RED);

        // Positionner le texte dans l'espace local du shape
        Runnable updateTextPosition = () -> {
            Bounds bounds = shape.getBoundsInLocal();
            // Calcul de la position locale avec les offsets
            double localX = bounds.getMinX() + bounds.getWidth() * offsetX;
            double localY = bounds.getMinY() + bounds.getHeight() * offsetY;

            // Centrer le texte
            label.setX(localX - label.getBoundsInLocal().getWidth() / 2);
            label.setY(localY + label.getBoundsInLocal().getHeight() / 4); // Ajustement vertical
        };

        // Mettre à jour la position du texte
        shape.boundsInLocalProperty().addListener((observable, oldValue, newValue) -> updateTextPosition.run());
        updateTextPosition.run();

        // Ajouter le texte au groupe
        group.getChildren().add(label);

        // Compensez l'échelle et la rotation pour isoler le texte
        group.localToSceneTransformProperty().addListener((observable, oldValue, newValue) -> {
            // Compensez la rotation
            double angle = group.getRotate();
            label.setRotate(-angle);

            // Compensez l'échelle
            double scale = 1 / drawingLayer.getScaleX();
            label.setScaleX(scale);
            label.setScaleY(scale);
        });

        return group;
    }*/
    
    private void addLabelToShape(String text, Shape shape, Group overlayGroup, Pane drawingLayer, double offsetX, double offsetY) {
    	 // Créer un texte avec une valeur par défaut
        Text label = new Text(text);

        // Définir la couleur et le style
        label.setFill(Color.RED);

        // Ajouter le label au groupe overlay
        overlayGroup.getChildren().add(label);

        // Méthode pour mettre à jour dynamiquement la position du texte
        Runnable updateLabelPosition = () -> {
        	// Obtenir les limites transformées (bounding box en coordonnées globales)
        	Point2D bounds = shape.localToScene(new Point2D(offsetX, offsetY));
            // Calculer la position cible avec des offsets
            double targetX = bounds.getX();
            double targetY = bounds.getY();
            // Centrer le texte par rapport à la position cible
            label.setX(targetX - label.getBoundsInLocal().getWidth() / 2);
            label.setY(targetY + label.getBoundsInLocal().getHeight() / 4); // Ajustement vertical pour le centrage
        };

        // Listener sur les transformations globales et locales
        
        // Si la shape subit une transformation on update la position des textes
        shape.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> updateLabelPosition.run());
        shape.localToParentTransformProperty().addListener((observable, oldValue, newValue) -> updateLabelPosition.run());
        
        // Si on zoom, ou qu'on translate a lors on doit déplacer les label de l'espace ecran
        drawingLayer.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> updateLabelPosition.run());
        drawingLayer.scaleXProperty().addListener((observable, oldValue, newValue) -> updateLabelPosition.run());
        drawingLayer.scaleYProperty().addListener((observable, oldValue, newValue) -> updateLabelPosition.run());
        drawingLayer.translateXProperty().addListener((observable, oldValue, newValue) -> updateLabelPosition.run());
        drawingLayer.translateYProperty().addListener((observable, oldValue, newValue) -> updateLabelPosition.run());

        // Forcer une première mise à jour
        updateLabelPosition.run();
    }

    
    private void clearSelection() {
        rectangleToFlotteurMap.forEach((rect, flotteur) -> rect.setFill(Color.BLUE));
        selectedFlotteurs.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Mise à jour des coordonnées de la souris
    private void updateMouseCoords(MouseEvent event, Pane drawingLayer, Label mouseCoordsLabel) {
        // Convertir les coordonnées de la scène vers celles du monde
        double worldX = drawingLayer.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
        double worldY = drawingLayer.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();

        // Mettre à jour le texte du label
        mouseCoordsLabel.setText(String.format("Coordonnées : (%.2f, %.2f)", worldX, worldY));
    }
    
    /**
     * Crée une flèche entre deux points avec des triangles aux extrémités et un texte au centre.
     *
     * @param startX    Coordonnée X de départ de la flèche
     * @param startY    Coordonnée Y de départ de la flèche
     * @param endX      Coordonnée X de fin de la flèche
     * @param endY      Coordonnée Y de fin de la flèche
     * @param arrowSize Taille des triangles aux extrémités
     * @param textValue Texte à afficher au centre de la flèche
     * @return Un groupe contenant la flèche complète
     */
    private Group createArrow(Pane drawingLayer, double startX, double startY, double endX, double endY, double arrowSize, String textValue) {
        Group arrowGroup = new Group();

        // Ligne de la flèche
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(1);

        // Calcul de l'angle de la ligne
        double angle = Math.atan2(endY - startY, endX - startX);

        // Triangle de départ
        Polygon startTriangle = createTriangle(startX, startY, angle + Math.PI, arrowSize);
        startTriangle.setStroke(Color.BLUE);
        startTriangle.setStrokeWidth(0.1);
        
        // Triangle de fin
        Polygon endTriangle = createTriangle(endX, endY, angle, arrowSize);
        endTriangle.setStroke(Color.BLUE);
        endTriangle.setStrokeWidth(0.1);

        
        // Texte au centre de la flèche
         /*
        Text distanceText = new Text((startX + endX) / 2, (startY + endY) / 2, textValue);
        distanceText.setFill(Color.RED);
        distanceText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        distanceText.setX(distanceText.getX() - distanceText.getLayoutBounds().getWidth() / 2);
        distanceText.setY(distanceText.getY() - distanceText.getLayoutBounds().getHeight() / 2);

        
        /*
        // Positionner le texte dans l'espace local du shape
        Runnable updateTextPosition = () -> {
            Bounds bounds = shape.getBoundsInLocal();

            // Calcul de la position locale avec les offsets
            double localX = bounds.getMinX() + bounds.getWidth() * offsetX;
            double localY = bounds.getMinY() + bounds.getHeight() * offsetY;

            // Centrer le texte
            label.setX(localX - label.getBoundsInLocal().getWidth() / 2);
            label.setY(localY + label.getBoundsInLocal().getHeight() / 4); // Ajustement vertical
        };*/
/*
        // Mettre à jour la position du texte
        //shape.boundsInLocalProperty().addListener((observable, oldValue, newValue) -> updateTextPosition.run());
        //updateTextPosition.run();

        // Ajouter le texte au groupe
        //group.getChildren().add(label);

        // Compensez l'échelle et la rotation pour isoler le texte
        arrowGroup.localToSceneTransformProperty().addListener((observable, oldValue, newValue) -> {
            // Compensez la rotation
            double angle4 = arrowGroup.getRotate();
            distanceText.setRotate(-angle4);

            // Compensez l'échelle
            double scale = 1 / drawingLayer.getScaleX();
            distanceText.setScaleX(scale);
            distanceText.setScaleY(scale);
        });
        */
        
        
        addLabelToShape("L.", line, overlayGroup, drawingLayer, (startX + endX) / 2, (startY + endY) / 2 - 2); 
        
        // Ajouter les éléments au groupe
        arrowGroup.getChildren().addAll(line, startTriangle, endTriangle);

        return arrowGroup;
    }

    /**
     * Crée un triangle pour représenter les extrémités de la flèche.
     *
     * @param x         Coordonnée X de la pointe du triangle
     * @param y         Coordonnée Y de la pointe du triangle
     * @param angle     Angle de rotation du triangle
     * @param size      Taille du triangle
     * @return Un objet Polygon représentant le triangle
     */
    private Polygon createTriangle(double x, double y, double angle, double size) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        // Points du triangle
        double x1 = x - size * cos + size * sin / 2;
        double y1 = y - size * sin - size * cos / 2;

        double x2 = x - size * cos - size * sin / 2;
        double y2 = y - size * sin + size * cos / 2;

        return new Polygon(x, y, x1, y1, x2, y2);
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

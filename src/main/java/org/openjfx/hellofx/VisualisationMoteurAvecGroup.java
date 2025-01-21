package org.openjfx.hellofx;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.ImageInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class VisualisationMoteurAvecGroup extends Application {

    private double lastX, lastY;
    private double startX, startY; // Origine constante pour le rectangle de sélection

    private double zoomFactor = 1.0;

    // Pour retenir les objets qui on été selection a partir de leur shape dans l'espace monde.
    private final HashMap<Shape, Flotteur> rectangleToFlotteurMap = new HashMap<>();
    private final Set<Flotteur> selectedFlotteurs = new HashSet<>();

    // Pour retenir le rectangle de selection et l'afficher
    private Rectangle selectionRectangle;
    
    // Calque pour l'espace monde (éléments dessinés)
    Group drawingGroup = new Group();
    // Calque pour l'overlay (éléments fixes comme les textes)
    Group overlayTextGroup = new Group();
    // Calque pour l'overlay (éléments fixes comme la sélection)
    Group overlaySelectionGroup = new Group();
    
    // Groupe general d'overilay
   // Group overlayGroup = new Group(overlaySelectionGroup, overlayTextGroup);

    // Calque pour les selection
    Group selectionOverlayGroup = new Group();

    // Pane racine contenant les deux calques
    Pane drawingLayer = new Pane(drawingGroup); // Contient l'espace monde
    Pane uiLayer = new Pane(overlayTextGroup, overlaySelectionGroup, selectionOverlayGroup); // Contient l'interface utilisateur
    Pane root = new Pane(drawingLayer, uiLayer);

    Group groupeRectangle = new Group();
    
    // Permet de savoir si on appuye sur SHIFT ou CTRL
	private boolean SHIFT;
	private boolean CTRL;
	private Paint colorSelection = Color.MAGENTA;
    
    @Override
    public void start(Stage primaryStage) {
        
    	Scene scene = createScene();
    	primaryStage.setTitle("Moteur de Visualisation avec Groupes et Calques");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

	public Scene createScene() {
		// Permet de mettre a jour les selection orange quand on zoom ou scroll
    	drawingLayer.localToSceneTransformProperty().addListener((observable, oldValue, newValue) -> updateSelectionOverlay());
    	// Style du fond
        root.setStyle("-fx-background-color: white;");
        
    /*    RotateTransition transition = new RotateTransition(Duration.seconds(15), groupeRectangle);
        transition.setFromAngle(0);
        transition.setToAngle(360);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.play();
    */    
        // Ici je vais dessiner ma scene avec des shape et associer un objet "metier"
        //Group groupeRectangle = new Group();
        groupeRectangle.setTranslateX(300);
        groupeRectangle.setRotate(45);
          
        Random rand = new Random();
        int cpt = 0;
     	// Ajouter des rectangles dans l'espace monde
        for (int i = 0; i < 32*1; i++) {
           for (int j = 0; j < 32*1; j++) {
        	   
        	   	// Groupe pour le shape, ainsi que d'eventuelle autre trucs (comme une fleche de mesure).
        		Group flotteurAll = new Group();
        		
        		// Deplace l'objet dans l'espace monde et lui fait subir une rotation
        		flotteurAll.setTranslateX(j*100);
    			flotteurAll.setTranslateY(i*100);
    			flotteurAll.setRotate((j+i)*10);
    			
    			/*
    			 * Si je mets ceci alors la selection ne sera pas animée...
    	        RotateTransition transition2 = new RotateTransition(Duration.seconds(15), flotteurAll);
    	        transition2.setFromAngle(0);
    	        transition2.setToAngle(360);
    	        transition2.setInterpolator(Interpolator.LINEAR);
    	        transition2.play();
    			 */
    			
    			Flotteur flotteur = new Flotteur("Flotteur " + j);
    			
    			
    			Shape rect = new Rectangle(-30, -20, 60, 40);
    			
    			
    			
    			
    			int rnd = i%9;
    			switch (rnd)
    			{
					case 0:
						rect = new Rectangle(-30, -20, 60, 40);
						break;
					case 1:
						rect = new Circle(-15, -15, 30);
						break;
					case 2:
						rect = new Circle(0, 0, 30);
						break;
					case 3:
						rect = new Polygon(0, -25, 55, 25, -25, 25);     // Triangle
						break;
					case 4:
						rect = new Line(-5, 0, 10, 0);
						break;
					case 5:
						rect = new Ellipse(10 , 5); 
						break;
					case 6:
						rect = new Ellipse(-10, -5, 20 ,10); 
						break;
					case 7:
						rect = new Arc(0, 0, 20, 20, 0, 270);
						((Arc)rect).setType(ArcType.ROUND); 
						break;
					case 8:
						rect = new Arc(-10, -5, 20, 10, 0, 200);
						((Arc)rect).setType(ArcType.ROUND);
						break;
					default:
	    				System.err.println("Mistake");
    					break;
    			}
    				   			

    			rect.setFill(Color.BLUE);
    			rect.setStroke(Color.BLACK);
    			
    		    			
    			Rectangle rect2 = new Rectangle(-35, -25, 3, 3);
    			rect2.setFill(Color.GREEN);
    			rect2.setStroke(Color.BLACK);
    			
    			//rect.getStyleClass().add("rectangle"); 
    			/*rect.setStyle(":hover {"
    					+ "    -fx-background-color: #383838;"
    					+ "    -fx-scale-y: 1.1;"
    					+ "}");*/
    			
    			
    			flotteurAll.getChildren().add(rect);
    			flotteurAll.getChildren().add(rect2);
    			
    			if (rnd == 0)
    			{
    			Group arrow = createArrow(drawingLayer, -30, -22, 30, -22, 3, "L");
    			flotteurAll.getChildren().add(arrow);
    			}
    		    
    			
    			// TODO : Comprendre pourquoi les transitions CSS ne fonctionne pas si on utilise cette methode
    			// Texte en espace ecran
    			addLabelToShape("D1_"+i+""+j, rect, overlayTextGroup, drawingLayer, -30, -20);
    			addLabelToShape("D2_"+i+""+j, rect, overlayTextGroup, drawingLayer, 0, 0);
    			addLabelToShape("D3_"+i+""+j, rect, overlayTextGroup, drawingLayer, 30, 20);
    		    
    		    // Texte dans l'espace monde
    		    Text label = new Text("WXYZ");
    		    label.setTextAlignment(TextAlignment.CENTER);
    		    label.setTextOrigin(VPos.CENTER);
    		    label.setStyle("-fx-text-alignment: center;");
    		    flotteurAll.getChildren().add(label);

    		    
    		    
    		    flotteurAll.getStyleClass().add("rectangle");
    		    
    		    groupeRectangle.getChildren().add(flotteurAll);
    			
    		    // Ajoute cette shape associer a l'objet metier dans la map des objets selectionnables. 
    		    rectangleToFlotteurMap.put(rect2, flotteur);
    		    rectangleToFlotteurMap.put(rect, flotteur);
    		    
    		}
        }
        
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
/*
    	    // Logs pour debug
    	    System.out.println("Zoom Factor: " + zoomFactor);
    	    System.out.println("Mouse Scene Position: (" + mouseSceneX + ", " + mouseSceneY + ")");
    	    System.out.println("TranslateX: " + drawingLayer.getTranslateX() + ", TranslateY: " + drawingLayer.getTranslateY());
    */	    
    	    if (zoomFactor>=0.2)
    	    {
    	    	overlayTextGroup.setVisible(true);
    	    }
    	    else
    	    {
    	    	overlayTextGroup.setVisible(false);
    	    }
    	    
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
                    overlaySelectionGroup.getChildren().add(selectionRectangle);
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

                if ((SHIFT==false) && (CTRL==false))
                	selectedFlotteurs.clear();
                
                selectionOverlayGroup.getChildren().clear(); // Efface les anciens indicateurs

                rectangleToFlotteurMap.forEach((shape, flotteur) -> {
                    if (Shape.intersect(selectionShape, shape).getBoundsInLocal().getWidth() > 0) {
                    	if ((CTRL==true) && (selectedFlotteurs.contains(flotteur)))
                   			selectedFlotteurs.remove(flotteur);
                    	else
                    		selectedFlotteurs.add(flotteur);
                    }
                });

                System.out.println("Flotteurs sélectionnés : " + selectedFlotteurs);

                overlaySelectionGroup.getChildren().remove(selectionRectangle);
                selectionRectangle = null;

                // Mettre à jour les formes dans l'espace écran
                updateSelectionOverlay();
            }
        });


        scene.setOnKeyPressed(event -> {
        	if (event.getCode() == KeyCode.ESCAPE) {
                clearSelection();
                System.out.println("Sélection annulée.");
            }
        	
        	if (event.getCode() == KeyCode.SHIFT) {
               SHIFT = true;
            }
        	if (event.getCode() == KeyCode.CONTROL) {
               CTRL = true;
            }
        	
        });
        
        scene.setOnKeyReleased(event -> {
        	if (event.getCode() == KeyCode.SHIFT) {
               SHIFT = false;
            }
        	if (event.getCode() == KeyCode.CONTROL) {
               CTRL = false;
        	}
        });
		return scene;
	}
    
    /*
    private void updateSelectionOverlay() {
        selectionOverlayGroup.getChildren().clear(); // Efface les anciennes formes

        // Recréer les formes pour chaque objet sélectionné
        rectangleToFlotteurMap.forEach((shape, flotteur) -> {
            if (selectedFlotteurs.contains(flotteur)) {
                // Obtenir les coordonnées globales de l'objet
                Bounds boundsInScene = shape.localToScene(shape.getBoundsInLocal());

                // Créer une forme visuelle correspondant à l'objet sélectionné
                Rectangle highlightShape = new Rectangle(
                    boundsInScene.getWidth(),
                    boundsInScene.getHeight()
                );

                // Positionner la forme dans l'espace écran
                highlightShape.setX(boundsInScene.getMinX());
                highlightShape.setY(boundsInScene.getMinY());

                // Appliquer un style visuel
                highlightShape.setFill(Color.ORANGE);
                highlightShape.setOpacity(0.25);

                // Ajouter la forme au groupe de surbrillance
                selectionOverlayGroup.getChildren().add(highlightShape);
            }
        });
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
    	/*
        rectangleToFlotteurMap.forEach((rect, flotteur) -> rect.setFill(Color.BLUE));
        selectedFlotteurs.clear();
        */
        //rectangleToFlotteurMap.forEach((rect, flotteur) -> rect.setFill(Color.BLUE));
        selectedFlotteurs.clear();
        selectionOverlayGroup.getChildren().clear();
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
         /* Cette portion du code est la meme que AddLabelToShape
        Text distanceText = new Text((startX + endX) / 2, (startY + endY) / 2, textValue);
        distanceText.setFill(Color.RED);
        distanceText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        distanceText.setX(distanceText.getX() - distanceText.getLayoutBounds().getWidth() / 2);
        distanceText.setY(distanceText.getY() - distanceText.getLayoutBounds().getHeight() / 2);

        
        
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
        
        
        addLabelToShape("L.", line, overlayTextGroup, drawingLayer, (startX + endX) / 2, (startY + endY) / 2 - 2); 
        
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
    
 

    private void updateSelectionOverlay() {
        selectionOverlayGroup.getChildren().clear(); // Efface les anciennes formes

        // Recréer les formes pour chaque objet sélectionné
        rectangleToFlotteurMap.forEach((shape, flotteur) -> {
            if (selectedFlotteurs.contains(flotteur)) {
                try {
                    // Créer une copie exacte de la Shape
                    Shape highlightShape = copyShape(shape);

                    if (highlightShape != null) {
                    	 // Appliquer explicitement la transformation globale
                        highlightShape.getTransforms().clear(); // Nettoyer les transformations existantes
                        highlightShape.getTransforms().add(shape.getLocalToSceneTransform());
                       // highlightShape.getTransforms().add(new Scale(1.2, 1.2, 1.0));
                        

                  /*      // Obtenir les coordonnées globales de l'objet
                        Bounds boundsInScene = shape.localToScene(shape.getBoundsInLocal());
                        highlightShape.setLayoutX(boundsInScene.getMinX());
                        highlightShape.setLayoutY(boundsInScene.getMinY());
*/
                        // Appliquer un style visuel spécifique
                        highlightShape.setFill(colorSelection);
                        highlightShape.setOpacity(0.75);

                        // Ajouter la copie au groupe de surbrillance
                        selectionOverlayGroup.getChildren().add(highlightShape);
                    }
                } catch (Exception e) {
                    System.err.println("Impossible de copier la forme : " + e.getMessage());
                }
            }
        });
    }
    
    private Shape copyShape(Shape original) {
        Shape copy = null;

        if (original instanceof Rectangle rect) {
        	copy = new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            
            
        } else if (original instanceof Circle circle) {
        	//copy = new Circle(circle.getRadius());
        	   copy = new Circle(circle.getCenterX(), circle.getCenterY(), circle.getRadius());
        } else if (original instanceof Ellipse ellipse) {
            //copy = new Ellipse(ellipse.getRadiusX(), ellipse.getRadiusY());
            copy = new Ellipse(ellipse.getCenterX(), ellipse.getCenterY(), ellipse.getRadiusX(), ellipse.getRadiusY());
        } else if (original instanceof Line line) {
            copy = new Line(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
        } else if (original instanceof Polygon polygon) {
            copy = new Polygon();
            ((Polygon) copy).getPoints().addAll(polygon.getPoints());
        } else if (original instanceof Path path) {
            copy = new Path();
            ((Path) copy).getElements().addAll(path.getElements());
        } else if (original instanceof Arc arc) {
            copy = new Arc();
            ((Arc) copy).setCenterX(arc.getCenterX());
            ((Arc) copy).setCenterY(arc.getCenterY());
            ((Arc) copy).setRadiusX(arc.getRadiusX());
            ((Arc) copy).setRadiusY(arc.getRadiusY());
            ((Arc) copy).setStartAngle(arc.getStartAngle());
            ((Arc) copy).setLength(arc.getLength());
            ((Arc) copy).setType(arc.getType());
        }

        if (copy != null) {
            // Copier les styles visuels de l'original
            //copy.setStroke(original.getStroke());
        	if ((original instanceof Line line))
            {
            	copy.setStroke(colorSelection );
            	copy.setStrokeWidth(original.getStrokeWidth()*2);
            }
            copy.setFill(original.getFill());
        }
      
        return copy;
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

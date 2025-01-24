package org.openjfx.hellofx;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

public class VisualisationMoteurAvecGroup_MERDERIE extends Application {

	// Pour fabrique le rectangle de selection, on note la ou l'on clique avec la souris et ou elle se trouve
    private double lastX, lastY;
    private double startX, startY; // Origine constante pour le rectangle de sélection

    private double zoomFactor = 1.0;

    // Pour retenir les objets qui on été selection a partir de leur shape dans l'espace monde.
    private final HashMap<Shape, Flotteur> ShapeToFlotteurMap = new HashMap<>();
    // Pour retenir les shape qui on été selection a partir de leur objet metier.
    private final HashMap<Flotteur, List<Shape>> FlotteurToShapeMap = new HashMap<>();
    
    private final Set<Flotteur> selectedFlotteurs = new HashSet<>();

    // Pour retenir le rectangle de selection et l'afficher
    private Rectangle selectionRectangle;
    
    // Calque pour l'espace monde (éléments dessinés)
    private Group drawingGroup = new Group();
    // Calque pour l'overlay (éléments fixes comme les textes)
    private Group overlayTextGroup = new Group();
    // Calque pour l'overlay (éléments fixes comme la sélection)
    private Group overlaySelectionGroup = new Group();
    
    
    // Pane racine contenant les deux calques
    private Pane drawingLayer = new Pane(drawingGroup); // Contient l'espace monde
    private Pane uiLayer = new Pane(overlayTextGroup,/* selectionOverlayGroup,*/ overlaySelectionGroup); // Contient l'interface utilisateur
    private Pane root = new Pane(drawingLayer, uiLayer);

    // Permet de savoir si on appuye sur SHIFT ou CTRL
	private boolean SHIFT;
	private boolean CTRL;
	private Paint colorSelection = Color.MAGENTA;
    
	// Les label que l'on retrouve en haut a gauche 
	private Label selectionCountLabel;
	private Label mouseCoordsLabel;
	
	
	private Scene scene;
	
	// Permet de changes les bouton si necessaire.
	MouseButton buttonSelection = MouseButton.PRIMARY;
	MouseButton buttonTranslation = MouseButton.MIDDLE;
	
    @Override
    public void start(Stage primaryStage) {
        
    	Scene scene = createScene();
    	primaryStage.setTitle("Moteur de Visualisation avec Groupes et Calques");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

	public Scene createScene() {
		// Permet de mettre a jour les selection orange quand on zoom ou scroll -> Avec le systeme de CSS on plus besoin de ça
    	// drawingLayer.localToSceneTransformProperty().addListener((observable, oldValue, newValue) -> updateSelectionOverlay());
    	
    
    	
    	// Style du fond
        root.setStyle("-fx-background-color: white;");
        /*
       RotateTransition transition = new RotateTransition(Duration.seconds(15), groupeRectangle);
        transition.setFromAngle(0);
        transition.setToAngle(360);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.play();
     */
        initializeObjectsToDraw();
        
        initalizeUiLayer();
        
        scene = new Scene(root, 800, 600);
        
        System.err.println(" >> " + getClass().getResource("/test.css"));
        // load and apply CSS. 
        Optional.ofNullable(getClass().getResource("/test.css")) 
                .map(URL::toExternalForm) 
                .ifPresent(scene.getStylesheets()::add); 
               
        // Mise à jour des coordonnées de la souris
        scene.setOnMouseMoved(event -> onMouseMouved(event));
            
        // Gestion de la souris au niveau du zoom avec la roulette
        scene.setOnScroll(event -> OnScroll(event));
   
        // Gestion de la souris, translation de la scene, ainsi que systeme de selection.
        scene.setOnMousePressed(event -> OnMousePressed(event));
        scene.setOnMouseDragged(event -> OnMouseDragged(event));  
        scene.setOnMouseReleased(event -> OnMouseReleased(event));

        // Gestion du clavier
        scene.setOnKeyPressed(event -> OnKeyPressed(event));
        scene.setOnKeyReleased(event -> OnKeyReleased(event));
        
        // Centrer la vue sur le point (0, 0)
        centerViewOnOrigin(drawingLayer, scene);
        
        // Recentre si on resize la fentre ??? Moi ca fait sauter le scroll...
        /*
        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            centerViewOnOrigin(drawingLayer, scene);
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            centerViewOnOrigin(drawingLayer, scene);
        });
        */
        
		return scene;
	}

	/**
	 * Crée les objets a dessiner dans la scene
	 */
	private void initializeObjectsToDraw() {
		 // Ici je vais dessiner ma scene avec des shape et associer un objet "metier"
        Group groupeRectangle = new Group();
        //groupeRectangle.setTranslateX(300);
        Transform e = Transform.translate(-450, -450);
        groupeRectangle.getTransforms().add(new Rotate(45));
        groupeRectangle.getTransforms().add(e);
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
    			// Si je mets ceci alors la selection ne sera pas animée...
    	        RotateTransition transition2 = new RotateTransition(Duration.seconds(15), flotteurAll);
    	        transition2.setFromAngle(0);
    	        transition2.setToAngle(360);
    	        transition2.setInterpolator(Interpolator.LINEAR);
    	        transition2.play();
    			 */
    			
    			Flotteur flotteur = new Flotteur("Flotteur " + j+"_"+i);
    			
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
    				Group arrow = createArrow(-30, -20-1, 30, -20-1, 3, "L");
    				flotteurAll.getChildren().add(arrow);
    			}
    		    
    			
    			// TODO : Comprendre pourquoi les transitions CSS ne fonctionne pas si on utilise cette methode
    			// Texte en espace ecran
    			addLabelToShape("HG_"+i+""+j, rect, /*overlayTextGroup, drawingLayer,*/ -30, -20);
    			addLabelToShape("CE_"+i+""+j, rect, /*overlayTextGroup, drawingLayer,*/ 0, 0);
    			addLabelToShape("BD_"+i+""+j, rect, /*overlayTextGroup, drawingLayer,*/ 30, 20);
    		    
    		    // Texte dans l'espace monde
    		    Text label = new Text("WXYZ");
    		    label.setTextAlignment(TextAlignment.CENTER);
    		    label.setTextOrigin(VPos.CENTER);
    		    label.setStyle("-fx-text-alignment: center;");
    		    label.setX(-label.getBoundsInLocal().getWidth()/2);
    		    
    		    flotteurAll.getChildren().add(label);
    		    
    		    //flotteurAll.getStyleClass().add("rectangle");
    		    // Debug
    		    //rect2.getStyleClass().add("rectangle");
    		    //rect.getStyleClass().add("rectangle");
    		    
    		    groupeRectangle.getChildren().add(flotteurAll);
    			
    		    // Ajoute cette shape associer a l'objet metier dans la map des objets selectionnables.
    		    addShapeToSelectable(rect2, flotteur);
    		    addShapeToSelectable(rect, flotteur);
    		}
        }
        
       
        // Dessine un point au centre de la scene/monde 0, 0
       
        Circle centeroftheworld = new Circle(30) ;
        centeroftheworld.setFill(Color.RED);
        addNodeToScene(centeroftheworld);
        
        
	}

	/**
	 * Cette méthode permet d'ajouter des groupes, des shape et nodes dans la scene a dessiner.
	 * @param node
	 */
	public void addNodeToScene(Node node) {
		drawingGroup.getChildren().add(node);
	}

	/**
	 * Initalize le layer Ui
	 */
	private void initalizeUiLayer() {
		// Label pour afficher les coordonnées de la souris
        mouseCoordsLabel = new Label("Coordonnées : (x, y)");
        selectionCountLabel = new Label("Objets sélectionnés : 0");
     
        VBox labelContainer = new VBox();
        labelContainer.setSpacing(5); // Espacement entre les labels
        labelContainer.setStyle(""
        		+ "-fx-background-color: rgba(255, 255, 255, 0.8);"
        		+ "-fx-padding: 10px;"
        		+ "-fx-border-color: black;"
        	    + "-fx-background-radius: 10px;"
        	    + "-fx-border-radius: 10px;"
        		+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 5);"
        		+ "-fx-font-size: 10px;");
        labelContainer.setLayoutX(10);
        labelContainer.setLayoutY(10); // Position globale du conteneur
        
        labelContainer.getChildren().add(selectionCountLabel);
        labelContainer.getChildren().add(mouseCoordsLabel);
        
        uiLayer.getChildren().add(labelContainer);
	}
    
	private void OnKeyPressed(KeyEvent event) {
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
    }
	
	
	private void OnKeyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.SHIFT) {
			SHIFT = false;
		}
		if (event.getCode() == KeyCode.CONTROL) {
			CTRL = false;
		}
		if (event.getCode() == KeyCode.R) {
			centerViewOnOrigin(drawingLayer, scene);
		}
	}

	/**
	 * Modifie les coordonées de la souris dans la label en haut a gauche
	 * @param event
	 */
	private void onMouseMouved(MouseEvent event) {
		updateMouseCoords(event, drawingLayer, mouseCoordsLabel);
	}

	/**
	 * Gestion du zoom avec la roulette de la souris pour zoomer dans la scene en utilisant le pointeur de la souris comme centre.
	 * @param event
	 */
	private void OnScroll(ScrollEvent event) {
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

		/*
		 * Zoom Factor: 0.7435517614520297 Mouse Scene Position: (426.0, 277.3333333333333) TranslateX: 246.2836903244558,
		 * TranslateY: 106.67353100911026
		 */

		if (zoomFactor >= 0.2) {
			overlayTextGroup.setVisible(true);
		} else {
			overlayTextGroup.setVisible(false);
		}

	}

	private void OnMousePressed(MouseEvent event) 
	{
		// Pour gerer la translation de la scène
		
		//if (event.getButton() == MouseButton.MIDDLE) {
		if (event.getButton() == buttonTranslation) {
		            lastX = event.getSceneX();
            lastY = event.getSceneY();
    		
        } else // Pour gerer le debut de selection
        //	if (event.getButton() == MouseButton.PRIMARY) {
           	if (event.getButton() == buttonSelection) {
            // Début du rectangle de sélection
            startX = event.getX();
            startY = event.getY();

            if (selectionRectangle == null) {
                selectionRectangle = new Rectangle();
                selectionRectangle.setFill(Color.color(1, 1, 0, 0.8));
                selectionRectangle.setStroke(Color.BLACK);
                overlaySelectionGroup.getChildren().add(selectionRectangle);
            }
            selectionRectangle.setX(startX);
            selectionRectangle.setY(startY);
            selectionRectangle.setWidth(0.000);
            selectionRectangle.setHeight(0.000);
        }	
	}
	
	private void OnMouseDragged(MouseEvent event) {
				
		// Pour gerer la translation de la scène
		//if (event.getButton() == MouseButton.MIDDLE) {
		if (event.getButton() == buttonTranslation) {
			double deltaX = event.getSceneX() - lastX;
			double deltaY = event.getSceneY() - lastY;
			drawingLayer.setTranslateX(drawingLayer.getTranslateX() + deltaX);
			drawingLayer.setTranslateY(drawingLayer.getTranslateY() + deltaY);
			lastX = event.getSceneX();
			lastY = event.getSceneY();
		} else
			// Pour gerer le debut de selection
		//	if (event.getButton() == MouseButton.PRIMARY && selectionRectangle != null) {
			if (event.getButton() == buttonSelection && selectionRectangle != null) {
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
	}
	
	private void OnMouseReleased(MouseEvent event) {

		// Pour gerer la fin de selection
		// if (event.getButton() == MouseButton.PRIMARY && selectionRectangle != null) {
		if (event.getButton() == buttonSelection && selectionRectangle != null) {

			// Si le rectangle de sélection est trop petit, simuler un clic simple
			double offsetFake = 0.001;

			// TODO : et pourquoi ne pas faire X - offsetFake en plus ? ;)
			selectionRectangle.setWidth(selectionRectangle.getWidth() + offsetFake);
			selectionRectangle.setHeight(selectionRectangle.getHeight() + offsetFake);

			// Convertir les limites du rectangle de sélection en coordonnées de l'espace monde
			double selectionX = selectionRectangle.getX();
			double selectionY = selectionRectangle.getY();
			double selectionWidth = selectionRectangle.getWidth();
			double selectionHeight = selectionRectangle.getHeight();

			// Rectangle de sélection comme un objet Shape
			Rectangle selectionShape = new Rectangle(selectionX, selectionY, selectionWidth, selectionHeight);

			if ((SHIFT == false) && (CTRL == false)) {
				clearSelection();
			}

			// selectionOverlayGroup.getChildren().clear(); // Efface les anciens indicateurs
			ShapeToFlotteurMap.forEach((shape, flotteur) -> {
				if (Shape.intersect(selectionShape, shape).getBoundsInLocal().getWidth() > 0) {
					if ((CTRL == true) /*&& (selectedFlotteurs.contains(flotteur))*/) {
						removeFromSelection(flotteur);
					} else {
						addToSelection(flotteur);
					}
				}
			});

			System.out.println("Flotteurs sélectionnés : " + selectedFlotteurs);
			overlaySelectionGroup.getChildren().remove(selectionRectangle);
			selectionRectangle = null;

			// Mettre à jour les formes dans l'espace écran -> plus necessaire avec le systeme de CSS
			// updateSelectionOverlay();
		}
	}

	/**
	 * Ajoute une forme aux objets selectionnable et associe son objet metier
	 * @param shape La forme Shape de l'objet representé
	 * @param flotteur L'objet Metier a representer
	 */
	protected void addShapeToSelectable(Shape rect, Flotteur flotteur) {
		ShapeToFlotteurMap.put(rect, flotteur);
		
		List<Shape> list = FlotteurToShapeMap.get(flotteur);
		if (list==null)
			list = new ArrayList<>();
		list.add(rect);
		
		FlotteurToShapeMap.put(flotteur, list);
	}
	
	/**
	 * Supprime le shape des cartes d'association des selections.
	 * @param rect2
	 */
	protected void removeShapeToSelectable(Shape rect2) {
		Flotteur flot = ShapeToFlotteurMap.get(rect2);
		ShapeToFlotteurMap.remove(rect2);
		FlotteurToShapeMap.remove(flot);
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
    
	/**
	 * Efface tout les objets de la selection
	 */
	private void clearSelection() {
    	// TODO : Pourquoi pas essayer d'appeler removeFromSelection
    	for (Iterator iterator = selectedFlotteurs.iterator(); iterator.hasNext();) {
			Flotteur flotteur = (Flotteur) iterator.next();
			
			//removeFromSelection(flotteur); -> concurrent exception
			// Helas repetition de la méthode removeFromSelection
			List<Shape> shapes = FlotteurToShapeMap.get(flotteur);
			for (int i = 0 ; i < shapes.size(); i++)
			{
				Shape shape = shapes.get(i);
				boolean ret = shape.getStyleClass().remove("ENGINE_ShapeSelected");
				//System.err.println("Remove style to "+shape.getClass()+" styles : "+shape.getStyleClass());
			}
		}
    	selectedFlotteurs.clear();
	}
    
	/**
	 * Ajouter un objet a la selection 
     * @param objet Un objet DTO metier a rajouter
	 */
    private void addToSelection(Flotteur objet) {
    	selectedFlotteurs.add(objet);
    	List<Shape> shapes = FlotteurToShapeMap.get(objet);
		for (int i = 0 ; i < shapes.size(); i++)
		{
			Shape shape = shapes.get(i);
			
			if (shape.getStyleClass().contains("ENGINE_ShapeSelected")==false)
			{
				boolean ret = shape.getStyleClass().add("ENGINE_ShapeSelected");
				//System.err.println("Add style to "+shape.getClass()+" styles : "+shape.getStyleClass());
			}
		}
	}

    /**
     * Supprime un objet de la selection
     * @param objet Un objet DTO metier a supprimer
     */
	private void removeFromSelection(Flotteur objet) {
		selectedFlotteurs.remove(objet);
		List<Shape> shapes = FlotteurToShapeMap.get(objet);
		for (int i = 0 ; i < shapes.size(); i++)
		{
			Shape shape = shapes.get(i);
			boolean ret = shape.getStyleClass().remove("ENGINE_ShapeSelected");
			//System.err.println("Remove style to "+shape.getClass()+" styles : "+shape.getStyleClass());
		}
	}

	/**
	 * Ajoute un text accroché a une shape (qui peut avoir subit des transformation d'espace). Ce texte sera affiche dans l'overlay des texte dans l'espace ecran et les positions
	 * seront recalculé si la "camera" est modifié
	 * @param text
	 * @param shape
	 * @param overlayGroup
	 * @param drawingLayer
	 * @param offsetX offset par rapport a point 0, 0 de la shape
	 * @param offsetY offset par rapport a point 0, 0 de la shape
	 */
	public void addLabelToShape(String text, Shape shape, /*Group overlayTextGroup, Pane drawingLayer,*/ double offsetX, double offsetY) {
		// TODO : Recevoir un objet texte et pas juste un string.
    	 // Créer un texte avec une valeur par défaut
        Text label = new Text(text);
        
        // Définir la couleur et le style
        label.setFill(Color.RED);

        //label.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        
        // Ajouter le label au groupe overlay
        overlayTextGroup.getChildren().add(label);

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
    public Group createArrow(double startX, double startY, double endX, double endY, double arrowSize, String textValue) {
        Group arrowGroup = new Group();

        // Ligne de la flèche
        
     // Calculate the center of the line
        double centerX = (startX + endX) / 2;
        double centerY = (startY + endY) / 2;

        // On triche un peu a cause du stroke sinon on voit un morceau de la ligne rectangulaire a la point des fleches
        // Scale the line's points towards the center
        double reductionFactor = 0.99;
        double startX_ = centerX + (startX - centerX) * reductionFactor;
        double startY_ = centerY + (startY - centerY) * reductionFactor;
        double endX_ = centerX + (endX - centerX) * reductionFactor;
        double endY_ = centerY + (endY - centerY) * reductionFactor;
        
        
        Line line = new Line(startX_, startY_, endX_, endY_);
        line.setFill(Color.BLACK);
        line.setStrokeLineJoin(StrokeLineJoin.MITER);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(0.2);

        // Calcul de l'angle de la ligne
        double angle = Math.atan2(endY - startY, endX - startX);

        // Triangle de départ
        Polygon startTriangle = createTriangle(startX, startY, angle + Math.PI, arrowSize);
        startTriangle.setFill(Color.RED);
        //startTriangle.setStroke(Color.BLUE);
        //startTriangle.setStrokeWidth(0.1);
        
        // Triangle de fin
        Polygon endTriangle = createTriangle(endX, endY, angle, arrowSize);
        endTriangle.setFill(Color.RED);
        //endTriangle.setStroke(Color.BLUE);
        //endTriangle.setStrokeWidth(0.1);

        
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
        
        
        addLabelToShape("L.", line,/* overlayTextGroup, drawingLayer,*/ (startX + endX) / 2, (startY + endY) / 2 - 2); 
        
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
    
 
    /**
     * Pour l'ancien system d'afficahge des selection. Je garde si le nouveau systeme de CSS merderait...
     */
    private void updateSelectionOverlay() {
    	//selectionOverlayGroup.getChildren().clear(); // Efface les anciennes formes

        // Recréer les formes pour chaque objet sélectionné
    	ShapeToFlotteurMap.forEach((shape, flotteur) -> {
            if (selectedFlotteurs.contains(flotteur)) {
                try {
                    // Créer une copie exacte de la Shape
                    Shape highlightShape = copyShape(shape);

                    if (highlightShape != null) {
                    	 // Appliquer explicitement la transformation globale
                        highlightShape.getTransforms().clear(); // Nettoyer les transformations existantes
                        highlightShape.getTransforms().add(shape.getLocalToSceneTransform());
                       // highlightShape.getTransforms().add(new Scale(1.2, 1.2, 1.0));
                        
                        // Appliquer un style visuel spécifique
                        highlightShape.setFill(colorSelection);
                        highlightShape.setOpacity(0.75);

                        // Ajouter la copie au groupe de surbrillance
                        //selectionOverlayGroup.getChildren().add(highlightShape);
                    }
                } catch (Exception e) {
                    System.err.println("Impossible de copier la forme : " + e.getMessage());
                }
            }
        });
        
        // Mettre à jour le label du nombre d'objets selectionnés
        selectionCountLabel.setText("Objets sélectionnés : " + selectedFlotteurs.size());

    }
    
    /**
     * N'est plus utilisé, je garde au cas ou l'affichag de selection pas CSS merderait.
     * @param original
     * @return
     */
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

    /**
     * Censé centre la scene a l'origine. Mais ne fonctionne que lorsque l'on crée la scene. Si on zoom ou qu'on translate alors ca déconne!!!!
     * @param drawingLayer
     * @param scene
     */
    private void centerViewOnOrigin(Pane drawingLayer, Scene scene) {
        double centerX = scene.getWidth() / 2;
        double centerY = scene.getHeight() / 2;

        drawingLayer.setTranslateX(centerX);
        drawingLayer.setTranslateY(centerY);
    }


    public static void main(String[] args) {
        launch(args);
    }
    
}

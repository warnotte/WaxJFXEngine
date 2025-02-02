package org.openjfx.hellofx;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public abstract class WFXPanel2D extends Pane {

	// TODO : Ces variable semble partage pour la selection et la translation ... ca me plait pas.
	// Pour fabrique le rectangle de selection, on note la ou l'on clique avec la souris et ou elle se trouve
    private double lastX, lastY;
    private double startX, startY; // Origine constante pour le rectangle de sélection

    private double zoomFactor = 1.0;

    // Pour retenir les objets qui on été selection a partir de leur shape dans l'espace monde.
    private final Map<Shape, Object> shapeToObjectMap = new WeakHashMap<>();
    // Pour retenir les shape qui on été selection a partir de leur objet metier.
    private final Map<Object, List<Shape>> objectToShapeMap = new WeakHashMap<>();
    
    // TODO : Je me demande si on devrait pas utilisé une liste pour les objets selectionnés pour garder l'ordre de selection. ?
    private final Set<Object> selectedObjects = new HashSet<>();

    // Pour retenir le rectangle de selection et l'afficher
    private Rectangle selectionRectangle;
    
    // Calque pour l'espace monde (éléments dessinés)
    private Group drawingGroup = new Group();
    // Calque pour l'overlay (éléments fixes comme les textes)
    private Group overlayTextGroup = new Group();
    // Calque pour l'overlay (éléments fixes comme la sélection)
    private Group overlaySelectionnRectangleGroup = new Group();
    
    // Calque pour l'overlay pour afficher un fleche de mesure.
    private Group overlayMeasureGroup = new Group();
    
    
    // Pane racine contenant les deux calques
    // TODO : Si on utilise drawLayer directement on dirait que ca fait bugger si je rotate le modele et que j'ai zoomé.
    private Group drawingLayer = new Group(); // Contient l'espace monde
    private Pane uiLayer = new Pane(); // Contient l'interface utilisateur

    // Permet de savoir si on appuye sur SHIFT ou CTRL
	private boolean SHIFT;
	private boolean CTRL;
	
	// Les label que l'on retrouve en haut a gauche 
	private Label selectionCountLabel;
	private Label mouseCoordsLabel;
	
	Point2D mousePositionInWorld = new Point2D(0, 0);
	Point2D mousePositionInScreen = new Point2D(0, 0);
	
	// Permet de changes les bouton si necessaire.
	MouseButton buttonSelection = MouseButton.PRIMARY;
	MouseButton buttonTranslation = MouseButton.MIDDLE;
	
	// Un groupe pour afficher la grille en arrière plan de tout.
	private Group gridGroup = new Group();

	// Variable pour le systeme de measure.
	private Point2D startMeasurePoint;
	private boolean isMeasuring = false;
	
	public WFXPanel2D()
	{
		super();
	    //private Pane root = new Pane(drawingGroup, overlayTextGroup, overlaySelectionGroup, uiLayer);
		
		
		
		getChildren().add(gridGroup);
		getChildren().add(drawingLayer);
		getChildren().add(overlayTextGroup);
		getChildren().add(overlaySelectionnRectangleGroup);
		getChildren().add(overlayMeasureGroup);
		getChildren().add(uiLayer);
		
		drawingLayer.getChildren().add(drawingGroup);
		
		// Permet d'eviter le probleme du deplacement du monde quand rotate.
		// Si on ne mets pas ca et qu'on rotate les telement de GUIStarter_Test1 ou GUIStarter_Test3 alors on voit que le monde se "promene"
		Shape fakeRect1 = new Rectangle (-10000, -10000, 0, 0);
		Shape fakeRect2 = new Rectangle ( 10000, -10000, 0, 0);
		Shape fakeRect3 = new Rectangle (-10000,  10000, 0, 0);
		Shape fakeRect4 = new Rectangle ( 10000,  10000, 0, 0);
		fakeRect1.setMouseTransparent(true);
		fakeRect2.setMouseTransparent(true);
		fakeRect3.setMouseTransparent(true);
		fakeRect4.setMouseTransparent(true);
		Group fakeGroupe = new Group();
		fakeGroupe.getChildren().add(fakeRect1);
		fakeGroupe.getChildren().add(fakeRect2);
		fakeGroupe.getChildren().add(fakeRect3);
		fakeGroupe.getChildren().add(fakeRect4);
		drawingLayer.getChildren().add(fakeGroupe);
		
		createScene();
		
		
	}
	
    

    private void createScene() {
		// Permet de mettre a jour les selection orange quand on zoom ou scroll -> Avec le systeme de CSS on plus besoin de ça
    	// drawingGroup.localToSceneTransformProperty().addListener((observable, oldValue, newValue) -> updateSelectionOverlay());
    	
		// Omets les évenement souris sur ces 2 calques car il masquent les evenement souris sur les evenement du "monde"
		overlayTextGroup.setMouseTransparent(true);
		overlaySelectionnRectangleGroup.setMouseTransparent(true);
		gridGroup.setMouseTransparent(true);
		overlayMeasureGroup.setMouseTransparent(true);
		
    	// Style du fond
        setStyle("-fx-background-color: white;");
        /*
       RotateTransition transition = new RotateTransition(Duration.seconds(15), groupeRectangle);
        transition.setFromAngle(0);
        transition.setToAngle(360);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.play();
     */
        
        initializeObjectsToDraw();
        initalizeUiLayer();
       
        drawingLayer.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {updateAllLabels(); createGrid();});
        drawingLayer.translateXProperty().addListener((obs, oldVal, newVal) -> {updateAllLabels(); createGrid();});
        drawingLayer.translateYProperty().addListener((obs, oldVal, newVal) -> {updateAllLabels(); createGrid();});
        drawingLayer.scaleXProperty().addListener((obs, oldVal, newVal) -> {updateAllLabels(); createGrid();});
        drawingLayer.scaleYProperty().addListener((obs, oldVal, newVal) -> {updateAllLabels(); createGrid();});
             
        System.err.println(" >> " + getClass().getResource("/FXWView2D.css"));
        // load and apply CSS. 
        Optional.ofNullable(getClass().getResource("/FXWView2D.css")) .map(URL::toExternalForm) .ifPresent(getStylesheets()::add); 
               
        // Mise à jour des coordonnées de la souris
        setOnMouseMoved(event -> onMouseMouved(event));
        // Gestion de la souris au niveau du zoom avec la roulette
        setOnScroll(event -> OnScroll(event));
        // Gestion de la souris, translation de la scene, ainsi que systeme de selection.
        setOnMousePressed(event -> OnMousePressed(event));
        setOnMouseDragged(event -> OnMouseDragged(event));  
        setOnMouseReleased(event -> OnMouseReleased(event));
        // Gestion du clavier
        setOnKeyPressed(event -> OnKeyPressed(event));
        setOnKeyReleased(event -> OnKeyReleased(event));


        // Attendre que la scène soit initialisée
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((obs, oldWidth, newWidth) -> createGrid());
                newScene.heightProperty().addListener((obs, oldHeight, newHeight) -> createGrid());
             
                createGrid(); // Appeler createGrid une fois la scène disponible
                centerViewOnOrigin();
            }
        });
        
        
    }
    
    
	
	public void doDummyDialog()
	{
	   Dialog<String> dialog = new Dialog<String>();
	      //Setting the title
	      dialog.setTitle("Dialog");
	      ButtonType type = new ButtonType("Ok", ButtonData.OK_DONE);
	      //Setting the content of the dialog
	      dialog.setContentText("What did i told you Robert!");
	      //Adding buttons to the dialog pane
	      dialog.getDialogPane().getButtonTypes().add(type);
	      //Setting the label
	      Text txt = new Text("Click the button to show the dialog");
	      Font font = Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 12);
	      txt.setFont(font);
	      //Creating a button
	      Button button = new Button("Show Dialog");
	      //Showing the dialog on clicking the button
	      button.setOnAction(e -> {
	         dialog.showAndWait();
	      });
	      //Creating a vbox to hold the button and the label
	      HBox pane = new HBox(15);
	      //Setting the space between the nodes of a HBox pane
	      pane.setPadding(new Insets(50, 150, 50, 60));
	      pane.getChildren().addAll(txt, button);
	      
	      dialog.show();
	}
	
	/**
	 * Ajoute un noeuds a la scene courante.
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
        labelContainer.getStyleClass().add("uiContainerTopLeft");
     /*   labelContainer.setStyle(""
        		+ "-fx-background-color: rgba(255, 255, 255, 0.8);"
        		+ "-fx-padding: 10px;"
        		+ "-fx-border-color: black;"
        	    + "-fx-background-radius: 10px;"
        	    + "-fx-border-radius: 10px;"
        		+ "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 5);"
        		+ "-fx-font-size: 10px;");*/
        labelContainer.setLayoutX(10);
        labelContainer.setLayoutY(10); // Position globale du conteneur
        
        Button button = new Button("Do not click ! :)");
        button.setOnMouseClicked(e -> {
        	System.err.println("Click on button on uilayer");
        	doDummyDialog();
        });
        
        labelContainer.getChildren().add(selectionCountLabel);
        labelContainer.getChildren().add(mouseCoordsLabel);
        labelContainer.getChildren().add(button);
        
        uiLayer.getChildren().add(labelContainer);
	}
    
	protected void OnKeyPressed(KeyEvent event) {
		// Attention que tant que la touche reste appuyé l'evement se répète.
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
    	
    	if ((event.getCode() == KeyCode.M) && (isMeasuring==false)) {
    		
    		System.err.println("START MEASURE");
    		
    		isMeasuring = true;
            overlayMeasureGroup.getChildren().clear();
            
            Point2D mouseScenePosition = new Point2D(mousePositionInWorld.getX(), mousePositionInWorld.getY());
            //startMeasurePoint = drawingLayer.sceneToLocal(mouseScenePosition);
            startMeasurePoint = /*drawingLayer.sceneToLocal*/(mouseScenePosition);
            
            System.err.println("Init pos in world : "+ startMeasurePoint);
        
            /*
            startMeasurePoint = new Point2D(mousePositionInWorld.getX(), mousePositionInWorld.getY());
            */
         }
    }
	
	protected void OnKeyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.SHIFT) {
			SHIFT = false;
		}
		if (event.getCode() == KeyCode.CONTROL) {
			CTRL = false;
		}
		/*
		if (event.getCode() == KeyCode.R) {
			centerViewOnOrigin(scene);
		}
		*/
		if (event.getCode() == KeyCode.C) {
			System.out.println("GC()");
			System.gc();
		}
		
		if (event.getCode() == KeyCode.R) {
			System.out.println("GC()");
			zoomFactor = 1.0;
			setZoom(1.0);
		}
	
		
		if (event.getCode() == KeyCode.M) {
    		isMeasuring = false;
            overlayMeasureGroup.getChildren().clear();
            startMeasurePoint = null;
         }
	}

	/**
	 * Modifie les coordonées de la souris dans la label en haut a gauche
	 * @param event
	 */
	protected void onMouseMouved(MouseEvent event) {
		//mousePositionInWorld = drawingLayer.sceneToLocal(event.getSceneX(), event.getSceneY());
		//mousePositionInScreen =  new Point2D(event.getSceneX(), event.getSceneY());
		// Mis a jour coordinée de la souris.
		updateMouseCoords(event.getSceneX(), event.getSceneY());
		
		if (isMeasuring && startMeasurePoint != null) {
	        updateMeasureArrow();
	    }
		
		
		
	}


	/**
	 * Gestion du zoom avec la roulette de la souris pour zoomer dans la scene en utilisant le pointeur de la souris comme centre.
	 * @param event
	 */
	protected void OnScroll(ScrollEvent event) {
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
		//System.out.println("Zoom Factor: " + zoomFactor);
		//System.out.println("Mouse Scene Position: (" + mouseSceneX + ", " + mouseSceneY + ")");
		//System.out.println("TranslateX: " + drawingLayer.getTranslateX() + ", TranslateY: " + drawingLayer.getTranslateY());

		/*
		 * Zoom Factor: 0.7435517614520297 Mouse Scene Position: (426.0, 277.3333333333333) TranslateX: 246.2836903244558,
		 * TranslateY: 106.67353100911026
		 */

		if (zoomFactor >= 0.2) {
			overlayTextGroup.setVisible(true);
		} else {
			overlayTextGroup.setVisible(false);
		}

		updateMeasureArrow(); // Mets a jour la fleche de mesure
		//createGrid(); // Mettre à jour la grille
	}

	protected void OnMousePressed(MouseEvent event) 
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
                overlaySelectionnRectangleGroup.getChildren().add(selectionRectangle);
            }
            selectionRectangle.setX(startX);
            selectionRectangle.setY(startY);
            selectionRectangle.setWidth(0.000);
            selectionRectangle.setHeight(0.000);
        }	
	}
	
	protected void OnMouseDragged(MouseEvent event) {
	
		
		// Pour gerer la translation de la scène
		//if (event.getButton() == MouseButton.MIDDLE) {
		if (event.getButton() == buttonTranslation) {
			double deltaX = event.getSceneX() - lastX;
			double deltaY = event.getSceneY() - lastY;
			drawingLayer.setTranslateX(drawingLayer.getTranslateX() + deltaX);
			drawingLayer.setTranslateY(drawingLayer.getTranslateY() + deltaY);
			lastX = event.getSceneX();
			lastY = event.getSceneY();
			
			updateMeasureArrow();
		    //createGrid(); // Mettre à jour la grille
			
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
		
		// Mis a jour coordinée de la souris.
		updateMouseCoords(event.getSceneX(), event.getSceneY());
				
	}
	
	protected void OnMouseReleased(MouseEvent event) {

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
			shapeToObjectMap.forEach((shape, flotteur) -> {
				if (Shape.intersect(selectionShape, shape).getBoundsInLocal().getWidth() > 0) {
					if ((CTRL == true) /*&& (selectedFlotteurs.contains(flotteur))*/) {
						removeFromSelection(flotteur);
					} else {
						addToSelection(flotteur);
					}
				}
			});

			System.out.println("Flotteurs sélectionnés : " + selectedObjects);
			overlaySelectionnRectangleGroup.getChildren().remove(selectionRectangle);
			selectionRectangle = null;

	    	// Mettre à jour le label
	    	selectionCountLabel.setText("Objets sélectionnés : " + selectedObjects.size());
	    	
			// Mettre à jour les formes dans l'espace écran -> plus necessaire avec le systeme de CSS
			// updateSelectionOverlay();
		}
	}
	
	public double setZoom() {
		return this.zoomFactor;
	}
	
	public void setZoom(double zoomFactor) {
		this.zoomFactor = zoomFactor;
		drawingLayer.setScaleX(zoomFactor);
		drawingLayer.setScaleY(zoomFactor);
		createGrid();
	}

	/**
	 * Ajoute une forme aux objets selectionnable et associe son objet metier
	 * @param shape La forme Shape de l'objet representé
	 * @param flotteur L'objet Metier a representer
	 */
	public void addShapeToSelectable(Shape rect, Flotteur flotteur) {
		shapeToObjectMap.put(rect, flotteur);
		
		List<Shape> list = objectToShapeMap.get(flotteur);
		if (list==null)
			list = new ArrayList<>();
		list.add(rect);
		
		objectToShapeMap.put(flotteur, list);
	}
	
	/**
	 * Supprime le shape des cartes d'association des selections.
	 * @param rect2
	 */
	public void removeShapeToSelectable(Shape rect2) {
		Object flot = shapeToObjectMap.get(rect2);
		shapeToObjectMap.remove(rect2);
		objectToShapeMap.remove(flot);
	}

	/**
	 * Efface tout les objets de la selection
	 */
	private void clearSelection() {
		
    	// TODO : Pourquoi pas essayer d'appeler removeFromSelection
    	for (Iterator<Object> iterator = selectedObjects.iterator(); iterator.hasNext();) {
			Object flotteur = iterator.next();
			
			//removeFromSelection(flotteur); -> concurrent exception
			// Helas repetition de la méthode removeFromSelection
			List<Shape> shapes = objectToShapeMap.get(flotteur);
			
			if (shapes==null)
	    	{
				System.err.println("clearSelection ("+flotteur+"): Avoid Null pointeur, c'est pas normal");
	    		break;
	    	}
			
			for (int i = 0 ; i < shapes.size(); i++)
			{
				Shape shape = shapes.get(i);
				boolean ret = shape.getStyleClass().remove("ENGINE_ShapeSelected");
				//System.err.println("Remove style to "+shape.getClass()+" styles : "+shape.getStyleClass());
			}
		}
    	selectedObjects.clear();
    
    	// Mettre à jour le label
    	selectionCountLabel.setText("Objets sélectionnés : " + selectedObjects.size());
	}
	

	/**
	 * Récuperes les objets selectionnés du type de la classe donnée
	 * @param <U> Le type de la classe
	 * @param class1 La classe de l'objet a récupérer (la même que U)
	 * @return
	 */
	public <U> List<U> getSelectedObjects(Class<U> class1) {
		List<U> list = new ArrayList<>();
		for (Iterator<Object> iterator = selectedObjects.iterator(); iterator.hasNext();) {
			Object object =  iterator.next();
			if (class1.isInstance(object)) {
				list.add((U) object);
			}
		}
		return list;
	}
	
	public Set<Object> getSelectedObjects() {
		return selectedObjects;
	}
    
	/**
	 * Ajouter un objet a la selection 
     * @param objet Un objet DTO metier a rajouter
	 */
    private void addToSelection(Object objet) {
    	selectedObjects.add(objet);
    	List<Shape> shapes = objectToShapeMap.get(objet);
    	
    	if (shapes==null)
    	{
    		System.err.println("ERROR : addToSelection ("+objet+"): Avoid Null pointeur, c'est pas normal");
    		return;
    	}
    	
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
	private void removeFromSelection(Object objet) {
		selectedObjects.remove(objet);
		List<Shape> shapes = objectToShapeMap.get(objet);
		for (int i = 0 ; i < shapes.size(); i++)
		{
			Shape shape = shapes.get(i);
			boolean ret = shape.getStyleClass().remove("ENGINE_ShapeSelected");
			//System.err.println("Remove style to "+shape.getClass()+" styles : "+shape.getStyleClass());
		}
	}

	/**
	 * Ajoute un text accroché a une shape (qui peut avoir subit des transformation d'espace). 
	 * Ce texte sera affiche dans l'overlay des texte dans l'espace ecran et les positions seront recalculé si la "camera" est modifié.
	 * Le texte est centré par defaut.
	 * @param text le texte
	 * @param shape la forme parente
	 * @param offsetX offset par rapport a point 0, 0 de la shape
	 * @param offsetY offset par rapport a point 0, 0 de la shape
	 */
	public void addLabelToShapeInScreenSpace(String text, Node shape, double offsetX, double offsetY) {
		Text label = new Text(text);
        // Définir la couleur et le style
        label.setFill(Color.BLACK);
        addLabelToShapeInScreenSpace(label, shape, offsetX, offsetY);
    }
	
	/**
	 * Ajoute un text accroché a une shape (qui peut avoir subit des transformation d'espace). 
	 * Ce texte sera affiche dans l'overlay des texte dans l'espace ecran et les positions seront recalculé si la "camera" est modifié.
	 * Le texte est centré par defaut.
	 * @param text le texte
	 * @param shape la forme parente
	 * @param offsetX offset par rapport a point 0, 0 de la shape
	 * @param offsetY offset par rapport a point 0, 0 de la shape
	 */
	public void addLabelToShapeInScreenSpace(Text label, Node shape, /*Group overlayTextGroup, Pane drawingLayer,*/ double offsetX, double offsetY) {

		label.setUserData(shape); // Associer la Shape au label pour un accès ultérieur
		label.getProperties().put("X", offsetX); // Garde ceci en mémoire pour .updateAllLabels()
		label.getProperties().put("Y", offsetY); // Garde ceci en mémoire pour .updateAllLabels()
		
		// Ajouter le label au groupe overlay
        overlayTextGroup.getChildren().add(label);

        // Méthode pour mettre à jour dynamiquement la position du texte
        Runnable updateLabelPosition = () -> {
        	
        	//System.err.println("UpdateLabelPosition() "+label);
        	
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
        shape.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {updateLabelPosition.run();/*System.err.println("layoutBoundsProperty() "+label);*/});
       // shape.localToParentTransformProperty().addListener((observable, oldValue, newValue) -> {updateLabelPosition.run();/*System.err.println("localToParentTransformProperty() "+label);*/});
        shape.localToSceneTransformProperty().addListener((observable, oldValue, newValue) -> {updateLabelPosition.run();/*System.err.println("localToSceneTransformProperty() "+label);*/});
        
        
 /*       
        // Si on zoom, ou qu'on translate a lors on doit déplacer les label de l'espace ecran
        drawingLayer.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> updateLabelPosition.run());
        drawingLayer.scaleXProperty().addListener((observable, oldValue, newValue) -> updateLabelPosition.run());
        drawingLayer.scaleYProperty().addListener((observable, oldValue, newValue) -> updateLabelPosition.run());
        drawingLayer.translateXProperty().addListener((observable, oldValue, newValue) -> updateLabelPosition.run());
        drawingLayer.translateYProperty().addListener((observable, oldValue, newValue) -> updateLabelPosition.run());
*/
        // Forcer une première mise à jour
  //      updateLabelPosition.run();
        // Mise à jour initiale de la position
        //Point2D bounds = shape.localToScene(Point2D.ZERO);
        updateLabelPosition.run();
        
	}
	
       // Mise à jour des coordonnées de la souris
    private void updateMouseCoords(double x, double y) {
        // Convertir les coordonnées de la scène vers celles du monde
    	//double worldX = drawingLayer.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
       // double worldY = drawingLayer.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();

		// Mise a jour de des positions de la souris.
		mousePositionInWorld = drawingGroup.sceneToLocal(x, y);
		mousePositionInScreen =  new Point2D(x, y);
    	
        // Mettre à jour le texte du label
        mouseCoordsLabel.setText(String.format("Coordonnées : (%.2f, %.2f) (%d, %d)", mousePositionInWorld.getX(), mousePositionInWorld.getY()
        		, (int)mousePositionInScreen.getX(), (int) mousePositionInScreen.getY()));
    }
    
    /**
     * Crée une flèche entre deux points avec des triangles aux extrémités et un texte au centre.
     * La fleche a les coordonées dans l'espace monde part rapport a son noeud parent.
     * Le texte sera affiché dans l'espace ecran dans groupe overlaytext
     *
     * @param startX    Coordonnée X de départ de la flèche
     * @param startY    Coordonnée Y de départ de la flèche
     * @param endX      Coordonnée X de fin de la flèche
     * @param endY      Coordonnée Y de fin de la flèche
     * @param arrowSize Taille des triangles aux extrémités
     * @param textValue Texte à afficher au centre de la flèche
     * @return Un groupe contenant la flèche complète
     */
    public Group createArrowInWorldSpaceWithTextInScreeSpace(double startX, double startY, double endX, double endY, double arrowSize, Text text) {
    	Group arrowGroup = createArrow(startX, startY, endX, endY, arrowSize);
        addLabelToShapeInScreenSpace(text, arrowGroup,/* overlayTextGroup, drawingLayer,*/ (startX + endX) / 2, (startY + endY) / 2 - 2); 
        return arrowGroup;
    }
    
    /**
     * Crée un fleche avec du texte dans l'espace ecran a partir des coordonées monde.
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @param arrowSize
     * @param textValue
     * @return
     */
    public Group createArrowInScreenSpaceWithTextInWorldSpace(Point2D start, Point2D end, double arrowSize, Text text) {
    	start = drawingLayer.localToScene(start);
    	end =  drawingLayer.localToScene(end);
    	Group arrowGroup = createArrow(start.getX(), start.getY(), end.getX(), end.getY(), arrowSize);
    	//Text text = new Text(textValue);
    	Point2D pt = new Point2D((start.getX() + end.getX()) / 2, (start.getY() + end.getY()) / 2 - 2);
    	text.setX(pt.getX());
    	text.setY(pt.getY());
    	arrowGroup.getChildren().add(text);
    	return arrowGroup;
    }
    
    /**
     * Crée une fleche 
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @param arrowSize
     * @return
     */
    public Group createArrow(double startX, double startY, double endX, double endY, double arrowSize) {
        Group arrowGroup = new Group();

        // Ligne de la flèche
        
     // Calculate the center of the line
        double centerX = (startX + endX) / 2;
        double centerY = (startY + endY) / 2;

        // On triche un peu a cause du stroke sinon on voit un morceau de la ligne rectangulaire a la point des fleches
        // Scale the line's points towards the center
        // TODO : Plus la fleche est grande, plus le reduction factor est influencé. C'est pas bon.
        double reductionFactor = 0.99;
        double startX_ = centerX + (startX - centerX) * reductionFactor;
        double startY_ = centerY + (startY - centerY) * reductionFactor;
        double endX_ = centerX + (endX - centerX) * reductionFactor;
        double endY_ = centerY + (endY - centerY) * reductionFactor;
        
        Line line = new Line(startX_, startY_, endX_, endY_);
        line.setFill(Color.BLACK);
        line.setStrokeLineJoin(StrokeLineJoin.MITER);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(0.5);
        
        // Calcul de l'angle de la ligne
        double angle = Math.atan2(endY - startY, endX - startX);

        // Triangle de départ
        Polygon startTriangle = createTriangle(startX, startY, angle + Math.PI, arrowSize);
        startTriangle.setFill(Color.BLACK);
        //startTriangle.setStroke(Color.BLUE);
        //startTriangle.setStrokeWidth(0.1);
        
        // Triangle de fin
        Polygon endTriangle = createTriangle(endX, endY, angle, arrowSize);
        endTriangle.setFill(Color.BLACK);
        //endTriangle.setStroke(Color.BLUE);
        //endTriangle.setStrokeWidth(0.1);

        // Ajouter les éléments au groupe
        arrowGroup.getChildren().addAll(line, startTriangle, endTriangle);
        //arrowGroup.getChildren().addAll(line);
        
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
     * Censé centre la scene a l'origine. Mais ne fonctionne que lorsque l'on crée la scene. Si on zoom ou qu'on translate alors ca déconne!!!!
     * @param drawingLayer
     * @param scene
     */
    public void centerViewOnOrigin() {
        double centerX = getScene().getWidth() / 2;
        double centerY = getScene().getHeight() / 2;

        drawingLayer.setTranslateX(centerX);
        drawingLayer.setTranslateY(centerY);
    }

    
    /**
     * Demande de reconstruire la scène quand on a ajouter ou supprime ou modifier des objets metiers.
     */
    protected void reinitializeScene() {
    	// TODO : ce truc va surement faire une mémory leak a cause des listener de drawingLayer.layoutBoundsProperty() et compagnie.
    	Set<Object> tempSelectedFlotteurs = new HashSet<>();
        // Sauvegarder les Flotteurs sélectionnés
        tempSelectedFlotteurs.clear();
        tempSelectedFlotteurs.addAll(selectedObjects);

    	
        // Vider le groupe de dessin
        drawingGroup.getChildren().clear();
        overlayTextGroup.getChildren().clear();
        overlaySelectionnRectangleGroup.getChildren().clear(); 
        shapeToObjectMap.clear();
        objectToShapeMap.clear();
        selectedObjects.clear();
        overlayMeasureGroup.getChildren().clear();
        
        // Réinitialiser les objets à dessiner
        initializeObjectsToDraw();
        
        // Restaurer la sélection
        for (Object flotteur : tempSelectedFlotteurs) {
            addToSelection(flotteur);
        }
        tempSelectedFlotteurs.clear();
        System.out.println("Sélection restaurée : " + selectedObjects);
        
        // Mettre à jour tous les labels après la recréation
        updateAllLabels();

        createGrid();
        // Log pour débogage
        System.out.println("La scène a été réinitialisée.");
    }
    
    /**
     * Mets a jour la position des label dans l'espace ecran de la couche overlayTextGroup quand on fait une translation
     * un zoom, ou que l'on reinitialize la scène.
     */
    private void updateAllLabels() {
    	System.err.println("Call updateLabel();");
        for (Node node : overlayTextGroup.getChildren()) {
            if (node instanceof Text label) {
            	Node associatedShape = (Node) label.getUserData(); // Récupérer la Shape associée
                if (associatedShape != null) {
                	double offsetX = (Double)label.getProperties().get("X");
                	double offsetY = (Double)label.getProperties().get("Y");
                    Point2D bounds = associatedShape.localToScene(new Point2D(offsetX, offsetY));
                    label.setX(bounds.getX() - label.getBoundsInLocal().getWidth() / 2);
                    label.setY(bounds.getY() + label.getBoundsInLocal().getHeight() / 4);
                }
            }
        }
    }
    
    
    
    public void createGrid() {
    	
        gridGroup.getChildren().clear(); // Nettoyer l'ancienne grille
         
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
        
        double rawCellSize = 50 / zoomFactor; // Calcul initial en fonction du zoom
        int multiple = 50; // Multiple auquel arrondir

        // Arrondir au multiple le plus proche
        baseCellSize = Math.round(rawCellSize / multiple) * multiple;
        
        baseCellSize = 5; // Espacement de référence à zoom = 1
        baseCellSize = baseCellSize * Math.pow(10, -Math.floor(Math.log10(zoomFactor)));
        
      //  System.err.println(">> " + baseCellSize);
        
        
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
        
        debugViewPort();
        
        Point2D pt;
        Line line;
        
        // Ne dessine pas a 100% de l'ecran si different de 0.
        double offsetBorder = 0;
        
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

    
	public void debugViewPort() {
		
		Scene scene = getScene();
        if (scene == null) return; // Si la scène n'est pas encore initialisée

        double sceneWidth = scene.getWidth();
        double sceneHeight = scene.getHeight();
        
        // Coins visibles de la scène (en pixels écran)
        Point2D topLeftScreen = new Point2D(0, 0); // Coin supérieur gauche
        Point2D bottomRightScreen = new Point2D(sceneWidth, sceneHeight); // Coin inférieur droit

		Point2D topLeftWorld = drawingLayer.sceneToLocal(topLeftScreen);
        Point2D bottomRightWorld = drawingLayer.sceneToLocal(bottomRightScreen);
        // Coordonnées min et max dans le monde
        double worldMinX = topLeftWorld.getX();
        double worldMinY = topLeftWorld.getY();
        double worldMaxX = bottomRightWorld.getX();
        double worldMaxY = bottomRightWorld.getY();
		
        System.out.println("World MinX: " + worldMinX + ", MinY: " + worldMinY);
        System.out.println("World MaxX: " + worldMaxX + ", MaxY: " + worldMaxY);
        System.out.println("Zoom : "+zoomFactor);
        System.out.println("Translate : "+drawingLayer.getTranslateX()+ " ; "+drawingLayer.getTranslateY());

		
	}



	/**
	 * Redessine la fleche de mesure apres un move, un drag, ou un zoom
	 */
	private void updateMeasureArrow() {
		if (isMeasuring==false)
			return;
		Point2D currentPoint = new Point2D(mousePositionInWorld.getX(), mousePositionInWorld.getY());
		overlayMeasureGroup.getChildren().clear();
		
		Point2D startCalc = startMeasurePoint; 
		Point2D endCalc = drawingLayer.sceneToLocal(new Point2D(currentPoint.getX(), currentPoint.getY())); 
		endCalc = (new Point2D(currentPoint.getX(), currentPoint.getY())); 
		double measureLength = endCalc.distance(startCalc);
		System.err.println("Coord Start : "+startCalc);
		System.err.println("Coord End : "+ endCalc);
		Group arrow = createArrowInScreenSpaceWithTextInWorldSpace(startCalc, endCalc, 10, new Text(""+String.format("%.2f", measureLength)));
		overlayMeasureGroup.getChildren().add(arrow);
		
		// Astuce a la con pour customisé la flèche de mesure
		String style = ""
				+ "	-fx-stroke-width: 1.0;"
				+ "	-fx-stroke: black;"
				+ "	-fx-fill: black;";
		
		for (int i = 0 ; i < arrow.getChildren().size(); i++)
			arrow.getChildren().get(i).setStyle(style);
		/*
		arrow.getChildren().get(0).setStyle(style);
		arrow.getChildren().get(1).setStyle(style);
		arrow.getChildren().get(2).setStyle(style);
		 */
	}
    
    
	protected abstract void initializeObjectsToDraw();
	
   
}

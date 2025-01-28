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

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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

public class VisualisationMoteurAvecGroup_BACKUP extends Pane {

	// TODO : Ces variable semble partage pour la selection et la translation ... ca me plait pas.
	// Pour fabrique le rectangle de selection, on note la ou l'on clique avec la souris et ou elle se trouve
    private double lastX, lastY;
    private double startX, startY; // Origine constante pour le rectangle de sélection

    private double zoomFactor = 1.0;

    // Pour retenir les objets qui on été selection a partir de leur shape dans l'espace monde.
    private final Map<Shape, Flotteur> ShapeToFlotteurMap = new WeakHashMap<>();
    // Pour retenir les shape qui on été selection a partir de leur objet metier.
    private final Map<Flotteur, List<Shape>> FlotteurToShapeMap = new WeakHashMap<>();
    
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
    private Pane uiLayer = new Pane(); // Contient l'interface utilisateur
    //private Pane root = new Pane(drawingLayer, overlayTextGroup, overlaySelectionGroup, uiLayer);

    // Permet de savoir si on appuye sur SHIFT ou CTRL
	private boolean SHIFT;
	private boolean CTRL;
	
	// Les label que l'on retrouve en haut a gauche 
	private Label selectionCountLabel;
	private Label mouseCoordsLabel;
	
	private Scene scene;
	
	// MON MODEL
	Flotteur [][] model = null;
	
	// Permet de changes les bouton si necessaire.
	MouseButton buttonSelection = MouseButton.PRIMARY;
	MouseButton buttonTranslation = MouseButton.MIDDLE;
	
	public VisualisationMoteurAvecGroup_BACKUP()
	{
		super();
		//drawingLayer, overlayTextGroup, overlaySelectionGroup, uiLayer
		getChildren().add(drawingLayer);
		getChildren().add(overlayTextGroup);
		getChildren().add(overlaySelectionGroup);
		getChildren().add(uiLayer);
		createScene();
	}
	
    

    private void createScene() {
		// Permet de mettre a jour les selection orange quand on zoom ou scroll -> Avec le systeme de CSS on plus besoin de ça
    	// drawingLayer.localToSceneTransformProperty().addListener((observable, oldValue, newValue) -> updateSelectionOverlay());
    	
		// Omets les évenement souris sur ces 2 calques car il masquent les evenement souris sur les evenement du "monde"
		overlayTextGroup.setMouseTransparent(true);
		overlaySelectionGroup.setMouseTransparent(true);
		
    	// Style du fond
        setStyle("-fx-background-color: white;");
        /*
       RotateTransition transition = new RotateTransition(Duration.seconds(15), groupeRectangle);
        transition.setFromAngle(0);
        transition.setToAngle(360);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.play();
     */
        createModel();
        
        initializeObjectsToDraw();
        initalizeUiLayer();
       
        drawingLayer.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> updateAllLabels());
        drawingLayer.scaleXProperty().addListener((obs, oldVal, newVal) -> updateAllLabels());
        drawingLayer.scaleYProperty().addListener((obs, oldVal, newVal) -> updateAllLabels());
        drawingLayer.translateXProperty().addListener((obs, oldVal, newVal) -> updateAllLabels());
        drawingLayer.translateYProperty().addListener((obs, oldVal, newVal) -> updateAllLabels());
       
        
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
      		
	}

	private void createModel() {
		model = new Flotteur[32][32];;
		   for (int i = 0; i < model.length; i++) {
	           for (int j = 0; j < model[i].length; j++) {
	        	   

	        	   // Création de l'objet métier Flotteur avec position et rotation
	        	   double x = j * 100;
	        	   double y = i * 100;
	        	   double rotation = (j + i) * 10; // Exemple de rotation
	        	   Flotteur flotteur = new Flotteur("Flotteur " + j + "_" + i, x, y, rotation);
	        	   model[i][j]=flotteur;
	           }
		   }
	}

	Group groupeRectangle;
	
	/**
	 * Crée les objets a dessiner dans la scene
	 */
	private void initializeObjectsToDraw() {
		 // Ici je vais dessiner ma scene avec des shape et associer un objet "metier"
		groupeRectangle = new Group();
        //groupeRectangle.setTranslateX(300);
        Transform e = Transform.translate(-450, -450);
        groupeRectangle.getTransforms().add(new Rotate(45));
        groupeRectangle.getTransforms().add(e);
        Random rand = new Random();
        int cpt = 0;
        
        
        
     	// Ajouter des rectangles dans l'espace monde
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[i].length; j++) {
         	   

         	   // Récuperation de l'objet metier
         	   Flotteur flotteur = model[i][j];

        	   
        	   //  Création du groupe de shapes associé au Flotteur
               Group flotteurAll = new Group();
               //flotteurAll.setId(flotteur.getName());
               flotteurAll.setTranslateX(flotteur.getX());
               flotteurAll.setTranslateY(flotteur.getY());
               flotteurAll.setRotate(flotteur.getRotation());
    			
              
               
     			// Ordinateur en PLS si activé.
    			/*
    	        RotateTransition transition2 = new RotateTransition(Duration.seconds(15), flotteurAll);
    	        transition2.setFromAngle(0);
    	        transition2.setToAngle(360);
    	        transition2.setInterpolator(Interpolator.LINEAR);
    	        transition2.play();
    			 */
    			

               	// Création d'une forme pour représenter visuellement le Flotteur
               	int rnd = i%9;
               	Shape rect = createShapeForFlotteur(rnd);

             	rect.setFill(Color.BLUE);
    			rect.setStroke(Color.BLACK);
    			// rect.setStrokeType(StrokeType.CENTERED); // Attention avec les lignes ...
    			
    			rect.setOnMouseEntered(event -> {
    				System.err.println("Entered");
    			});
    			rect.setOnMouseExited(event -> {
    				System.err.println("Exited");
    			});
    			rect.setOnMouseClicked(event -> {
    				System.err.println("Clicked");
    			});		
    			
    			Rectangle rect2 = new Rectangle(-35, -25, 3, 3);
    			rect2.setFill(Color.GREEN);
    			rect2.setStroke(Color.BLACK);
    			rect2.setStrokeType(StrokeType.INSIDE);

    			
    			rect.getStyleClass().add("rectangle"); 
    			/*rect.setStyle(":hover {"
    					+ "    -fx-background-color: #383838;"
    					+ "    -fx-scale-y: 1.1;"
    					+ "}");*/
    			
    			flotteurAll.getChildren().add(rect);
    			flotteurAll.getChildren().add(rect2);
    			// Ajout d'un label pour représenter le nom du Flotteur (en espace écran)
                addLabelToShapeInScreenSpace(flotteur.getName(), rect, 0, 0);

    			if (rnd == 0)
    			{
    				Group arrow = createArrow(-30, -20-1, 30, -20-1, 3, "L");
    				flotteurAll.getChildren().add(arrow);
    			}
    		    
    			
    			// Texte en espace ecran
    			Text label2 = new Text("BD_"+i+""+j);
    	        label2.setFill(Color.GREEN);
    			addLabelToShapeInScreenSpace(label2, rect, /*overlayTextGroup, drawingLayer,*/ 30, 20);
    			addLabelToShapeInScreenSpace("HG_"+i+""+j, rect, /*overlayTextGroup, drawingLayer,*/ -30, -20);
    			addLabelToShapeInScreenSpace("CE_"+i+""+j, rect, /*overlayTextGroup, drawingLayer,*/ 0, 0);
    		    
    		    // Texte dans l'espace monde -> addLAbelToShapeInWordSpace... ?? :)
    		    Text label = new Text("WXYZ");
    		    label.setMouseTransparent(true);
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
    		    //addShapeToSelectable(flotteurAll, flotteur);
    		    /*
                TextField tf = new TextField();
                tf.setText("Y/N");
                flotteurAll.getChildren().add(tf);
                */
    		    
    		}
        }
        
        // Affiche une fleche sur le groupe entier.
        double w = groupeRectangle.getBoundsInLocal().getWidth();
        Group arrow = createArrow(0, -100, w, -100, 3, "700");
        
        for (Iterator<Node> iterator = arrow.getChildren().iterator(); iterator.hasNext();) {
			Node flotteur2 = iterator.next();
			flotteur2.setStyle(""
	        		+ "-fx-stroke: green;"
	        		+ "-fx-stroke-width: 2px;"
	        		+ "");
		}

        
        groupeRectangle.getChildren().add(arrow);
        
        // Affiche un texte ui sur le groupe entier.
        addLabelToShapeInScreenSpace("MIDDLE OF THE WORLD minus Y100", groupeRectangle, /*overlayTextGroup, drawingLayer,*/ 0, -100);
        
        
        addNodeToScene(groupeRectangle);
        //drawingGroup.getChildren().add(groupeRectangle);           
       
        Circle centeroftheworld = new Circle(30) ;
        centeroftheworld.setFill(Color.RED);
       // drawingGroup.getChildren().add(centeroftheworld);
        addNodeToScene(centeroftheworld);


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
	
	// Méthode utilitaire pour créer différentes formes
	private Shape createShapeForFlotteur(int i) {
	    switch (i % 9) {
	    	case 0: return new Rectangle(-30, -20, 60, 40);
	        case 1: return new Circle(-15, -15, 30);
	        case 2: return new Circle(0, 0, 30);
	        case 3: return new Polygon(0, -25, 55, 25, -25, 25); // Triangle
	        case 4: return new Line(-5, 0, 10, 0);
	        case 5: return new Ellipse(10, 5);
	        case 6: return new Ellipse(-10, -5, 20, 10);
	        case 7: {
	            Arc arc = new Arc(0, 0, 20, 20, 0, 270);
	            arc.setType(ArcType.ROUND);
	            return arc;
	        }
	        case 8: {
	            Arc arc = new Arc(-10, -5, 20, 10, 0, 200);
	            arc.setType(ArcType.ROUND);
	            return arc;
	        }
	        default: return new Rectangle(-30, -20, 60, 40);
	    }
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
	
	protected void OnKeyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.SHIFT) {
			SHIFT = false;
		}
		if (event.getCode() == KeyCode.CONTROL) {
			CTRL = false;
		}
		if (event.getCode() == KeyCode.R) {
			centerViewOnOrigin(scene);
		}
		if (event.getCode() == KeyCode.G) {
			System.out.println("Touche G appuyée.");
			moveSelectedFlotteursRandomly();
		}
		if (event.getCode() == KeyCode.C) {
			System.out.println("GC()");
			System.gc();
		}
		
		if (event.getCode() == KeyCode.O) {
			groupeRectangle.setRotate(groupeRectangle.getRotate()+1);

		}
		if (event.getCode() == KeyCode.P) {
			groupeRectangle.setRotate(groupeRectangle.getRotate()-1);

		}
		
		
		
	}

	/**
	 * Modifie les coordonées de la souris dans la label en haut a gauche
	 * @param event
	 */
	protected void onMouseMouved(MouseEvent event) {
		updateMouseCoords(event, drawingLayer, mouseCoordsLabel);
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
                overlaySelectionGroup.getChildren().add(selectionRectangle);
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

			// Mettre à jour le label
	    	selectionCountLabel.setText("Objets sélectionnés : " + selectedFlotteurs.size());
	    	
			// Mettre à jour les formes dans l'espace écran -> plus necessaire avec le systeme de CSS
			// updateSelectionOverlay();
		}
	}

	/**
	 * Ajoute une forme aux objets selectionnable et associe son objet metier
	 * @param shape La forme Shape de l'objet representé
	 * @param flotteur L'objet Metier a representer
	 */
	public void addShapeToSelectable(Shape rect, Flotteur flotteur) {
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
	public void removeShapeToSelectable(Shape rect2) {
		Flotteur flot = ShapeToFlotteurMap.get(rect2);
		ShapeToFlotteurMap.remove(rect2);
		FlotteurToShapeMap.remove(flot);
	}

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
    	
    	// Mettre à jour le label
    	selectionCountLabel.setText("Objets sélectionnés : " + selectedFlotteurs.size());
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

        addLabelToShapeInScreenSpace(textValue, line,/* overlayTextGroup, drawingLayer,*/ (startX + endX) / 2, (startY + endY) / 2 - 2); 
        
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
     * Censé centre la scene a l'origine. Mais ne fonctionne que lorsque l'on crée la scene. Si on zoom ou qu'on translate alors ca déconne!!!!
     * @param drawingLayer
     * @param scene
     */
    public void centerViewOnOrigin(Scene scene) {
        double centerX = scene.getWidth() / 2;
        double centerY = scene.getHeight() / 2;

        drawingLayer.setTranslateX(centerX);
        drawingLayer.setTranslateY(centerY);
    }

    
    /**
     * Demande de reconstruire la scène quand on a ajouter ou supprime ou modifier des objets metiers.
     */
    protected void reinitializeScene() {
    	// TODO : ce truc va surement faire une mémory leak a cause des listener de drawingLayer.layoutBoundsProperty() et compagnie.
    	Set<Flotteur> tempSelectedFlotteurs = new HashSet<>();
        // Sauvegarder les Flotteurs sélectionnés
        tempSelectedFlotteurs.clear();
        tempSelectedFlotteurs.addAll(selectedFlotteurs);

    	
        // Vider le groupe de dessin
        drawingGroup.getChildren().clear();
        overlayTextGroup.getChildren().clear();
        overlaySelectionGroup.getChildren().clear(); 
        ShapeToFlotteurMap.clear();
        FlotteurToShapeMap.clear();
        selectedFlotteurs.clear();
        
        // Réinitialiser les objets à dessiner
        initializeObjectsToDraw();
        
        // Restaurer la sélection
        for (Flotteur flotteur : tempSelectedFlotteurs) {
            addToSelection(flotteur);
        }
        tempSelectedFlotteurs.clear();
        System.out.println("Sélection restaurée : " + selectedFlotteurs);
        
        
        // Mettre à jour tous les labels après la recréation
        updateAllLabels();
        
        
        // Log pour débogage
        System.out.println("La scène a été réinitialisée.");
    }
    
    /**
     * Mets a jour la position des label dans l'espace ecran de la couche overlayTextGroup quand on fait une translation
     * un zoom, ou que l'on reinitialize la scène.
     */
    private void updateAllLabels() {
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
    
    private void moveSelectedFlotteursRandomly() {
        Random random = new Random();

        // Parcourir les objets sélectionnés
        for (Flotteur flotteur : selectedFlotteurs) {
            // Modifier la position du Flotteur de manière aléatoire
            double deltaX = random.nextDouble() * 100 - 50; // Valeur entre -5 et +5
            double deltaY = random.nextDouble() * 100 - 50;

            flotteur.setX(flotteur.getX() + deltaX);
            flotteur.setY(flotteur.getY() + deltaY);

            
        }
        // Mettre à jour le groupe visuel correspondant
        reinitializeScene();
    }
    
   
}

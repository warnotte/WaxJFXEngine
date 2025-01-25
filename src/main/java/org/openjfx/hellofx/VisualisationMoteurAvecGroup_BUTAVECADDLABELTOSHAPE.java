package org.openjfx.hellofx;

import java.net.URL;
import java.util.Optional;
import java.util.Random;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

public class VisualisationMoteurAvecGroup_BUTAVECADDLABELTOSHAPE extends Application {

	// Pour fabrique le rectangle de selection, on note la ou l'on clique avec la souris et ou elle se trouve
	private double lastX, lastY;

	private double zoomFactor = 1.0;

	// Calque pour l'espace monde (éléments dessinés)
	private Group drawingGroup = new Group();
	// Calque pour l'overlay (éléments fixes comme les textes)
	private Group overlayTextGroup = new Group();
	// Calque pour l'overlay (éléments fixes comme la sélection)
	private Group overlaySelectionGroup = new Group();

	// Pane racine contenant les deux calques
	private Pane drawingLayer = new Pane(drawingGroup); // Contient l'espace monde
	//private Pane uiLayer = new Pane(overlayTextGroup, overlaySelectionGroup); // Contient l'interface utilisateur
	
	// TODO : Si j'inverse ces 2 choses, alors les event fonctionne sur les rectangle... mais le texte est affiché par dessous :|
	private Pane root = new Pane(drawingLayer, overlaySelectionGroup, overlayTextGroup);

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
		// drawingLayer.localToSceneTransformProperty().addListener((observable, oldValue, newValue) ->
		// updateSelectionOverlay());
		
		// TODO : mmmmmm y'a un mix foireux... faut ptet pas avoir uiLayer ??? et mettre les 3 truc dans rootPane
		overlayTextGroup.setMouseTransparent(true); // Assurez-vous que cette couche est transparente pour les événements
	//	uiLayer.setMouseTransparent(true); // Assurez-vous que cette couche est transparente pour les événements

		// Style du fond
		root.setStyle("-fx-background-color: white;");

		initializeObjectsToDraw();

		scene = new Scene(root, 800, 600);

		System.err.println(" >> " + getClass().getResource("/test.css"));
		// load and apply CSS.
		Optional.ofNullable(getClass().getResource("/test.css")).map(URL::toExternalForm).ifPresent(scene.getStylesheets()::add);

		// Gestion de la souris au niveau du zoom avec la roulette
		scene.setOnScroll(event -> OnScroll(event));

		// Gestion de la souris, translation de la scene, ainsi que systeme de selection.
		scene.setOnMousePressed(event -> OnMousePressed(event));
		scene.setOnMouseDragged(event -> OnMouseDragged(event));

		return scene;
	}

	/**
	 * Crée les objets a dessiner dans la scene
	 */
	private void initializeObjectsToDraw() {
		// Ici je vais dessiner ma scene avec des shape et associer un objet "metier"
		Group groupeRectangle = new Group();
		// groupeRectangle.setTranslateX(300);
		Transform e = Transform.translate(-450, -450);
		groupeRectangle.getTransforms().add(new Rotate(45));
		groupeRectangle.getTransforms().add(e);
		Random rand = new Random();
		int cpt = 0;
		// Ajouter des rectangles dans l'espace monde
		for (int i = 0; i < 32 * 1; i++) {
			for (int j = 0; j < 32 * 1; j++) {

				// Groupe pour le shape, ainsi que d'eventuelle autre trucs (comme une fleche de mesure).
				Group flotteurAll = new Group();

				// Deplace l'objet dans l'espace monde et lui fait subir une rotation
				flotteurAll.setTranslateX(j * 100);
				flotteurAll.setTranslateY(i * 100);
				flotteurAll.setRotate((j + i) * 10);

				Flotteur flotteur = new Flotteur("Flotteur " + j + "_" + i, 0, 0, 0);

				Shape rect = new Rectangle(-30, -20, 60, 40);
				// TODO : Ceci e fonctione pas a partir du moment ou on appelle addLabelToShape
				rect.getStyleClass().add("rectangle");
				rect.setStyle(":hover {" + "    -fx-background-color: #383838;" + "    -fx-scale-y: 1.1;" + "}");

				rect.setFill(Color.BLUE);
				rect.setStroke(Color.BLACK);

				// TODO : C'est Event ne fonctione pas a partir du moment ou on appelle addLabelToShape
				rect.setOnMouseEntered(event -> {
					System.err.println("Entered");
				});
				rect.setOnMouseExited(event -> {
					System.err.println("Exited");
				});
				rect.setOnMouseClicked(event -> {
					System.err.println("Clicked");
				});

				flotteurAll.getChildren().add(rect);

				// TODO : Comprendre pourquoi les transitions CSS ne fonctionne pas si on utilise cette methode.
				// TODO : Cela a surement un rapport avec les rect.setOnMouseEntered qui ne sont pas fonctionnel non plus.
				addLabelToShape("HG_" + i + "" + j, rect, -30, -20);
				addLabelToShape("CE_" + i + "" + j, rect, 0, 0);
				addLabelToShape("BD_" + i + "" + j, rect, 30, 20);

				groupeRectangle.getChildren().add(flotteurAll);
			}
		}
		drawingGroup.getChildren().add(groupeRectangle);
	}

	/**
	 * Gestion du zoom avec la roulette de la souris pour zoomer dans la scene en utilisant le pointeur de la souris comme
	 * centre.
	 * 
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

	private void OnMousePressed(MouseEvent event) {
		// Pour gerer la translation de la scène

		// if (event.getButton() == MouseButton.MIDDLE) {
		if (event.getButton() == buttonTranslation) {
			lastX = event.getSceneX();
			lastY = event.getSceneY();
		}
	}

	private void OnMouseDragged(MouseEvent event) {

		// Pour gerer la translation de la scène
		// if (event.getButton() == MouseButton.MIDDLE) {
		if (event.getButton() == buttonTranslation) {
			double deltaX = event.getSceneX() - lastX;
			double deltaY = event.getSceneY() - lastY;
			drawingLayer.setTranslateX(drawingLayer.getTranslateX() + deltaX);
			drawingLayer.setTranslateY(drawingLayer.getTranslateY() + deltaY);
			lastX = event.getSceneX();
			lastY = event.getSceneY();
		}
	}

	/**
	 * Ajoute un text accroché a une shape (qui peut avoir subit des transformation d'espace). Ce texte sera affiche dans
	 * l'overlay des texte dans l'espace ecran et les positions seront recalculé si la "camera" est modifié
	 * 
	 * @param text
	 * @param shape
	 * @param overlayGroup
	 * @param drawingLayer
	 * @param offsetX      offset par rapport a point 0, 0 de la shape
	 * @param offsetY      offset par rapport a point 0, 0 de la shape
	 */
	public void addLabelToShape(String text, Shape shape, double offsetX, double offsetY) {
		// Créer un texte avec une valeur par défaut
		Text label = new Text(text);
		label.setMouseTransparent(true);
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
			// TODO : CHATGPT : Si je commente ces 2 lignes, je ne sais pas pourquoi mais alors le problème disparait
			label.setX(targetX - label.getBoundsInLocal().getWidth() / 2);
			label.setY(targetY + label.getBoundsInLocal().getHeight() / 4); // Ajustement vertical pour le centrage*/
		};
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

	public static void main(String[] args) {
		launch(args);
	}

}

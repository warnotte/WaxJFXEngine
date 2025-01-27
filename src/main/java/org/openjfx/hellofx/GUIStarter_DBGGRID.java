package org.openjfx.hellofx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GUIStarter_DBGGRID extends Application {

	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {

		VisualisationMoteurAvecGroup_GRID root = new VisualisationMoteurAvecGroup_GRID();

		BorderPane pane = new BorderPane();
		pane.setCenter(root);

		pane.getStyleClass().add("uiContainerTopLeft");

		Scene scene = new Scene(pane, 800, 600);
		// scene = new Scene(root, 800, 600);
		// Centrer la vue sur le point (0, 0)

		//root.centerViewOnOrigin(scene);
		// root.scene= scene;

		primaryStage.setTitle("Moteur de Visualisation avec Groupes et Calques");
		primaryStage.setScene(scene);
		primaryStage.show();

		// ScenicView.show(scene);

	}
}
package org.openjfx.hellofx;

import java.util.Random;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GUIStarter_SImpleTestRotate extends Application {

	public static void main(final String[] args) {
    	launch(args);
    }

	Group mongroupe;
	
    @Override
    public void start(Stage primaryStage) {
        
      //  pane.getStyleClass().add("uiContainerTopLeft");
        Group root = new Group();
    	
        mongroupe = new Group();
		Random rand = new Random();
		for (int i = -20; i <= 20; i++) {
			for (int j = -20; j <= 20; j++) {
				Rectangle rect = new Rectangle(-25, -25, 50, 50);
				rect.setTranslateX(i * 100);
				rect.setTranslateY(j * 100);
				rect.setFill(Color.BLUE);
				rect.setStroke(Color.BLACK);
				mongroupe.getChildren().add(rect);
			}
		}
		mongroupe.setScaleX(0.1);
		mongroupe.setScaleY(0.1);
		mongroupe.setRotate(45);
		
		mongroupe.setTranslateX(400);
		mongroupe.setTranslateY(300);
		
		root.getChildren().add(mongroupe);		
        
		root.setOnKeyReleased(event -> OnKeyReleased(event));
        
        Scene scene = new Scene(root, 800, 600);
        
        scene.setOnKeyReleased(event -> OnKeyReleased(event));
        //scene = new Scene(root, 800, 600);
        // Centrer la vue sur le point (0, 0)
        
        //root.centerViewOnOrigin(scene);
        //root.scene= scene;
    	
        primaryStage.setTitle("Moteur de Visualisation avec Groupes et Calques");
        primaryStage.setScene(scene);
        primaryStage.show();
        
       // ScenicView.show(scene); 

    }
    
    protected void OnKeyReleased(KeyEvent event) {
	
	
		if (event.getCode() == KeyCode.O) {
			mongroupe.setRotate(mongroupe.getRotate()+1);
			//createGrid();
			//reinitializeScene();
		}
		if (event.getCode() == KeyCode.P) {
			mongroupe.setRotate(mongroupe.getRotate()-1);
			//createGrid();
			//reinitializeScene();
		}
		
	}
}
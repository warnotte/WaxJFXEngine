package org.openjfx.hellofx;

import java.util.Random;

import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class UserViewer_Test2 extends WFXPanel2D {

	// -Dprism.order=sw
	Group mongroupe;
	
	@Override
	protected void initializeObjectsToDraw() {
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
		
		//mongroupe.setRotate(45);
		
		
		addNodeToScene(mongroupe);
		addNodeToScene(new Circle(5));
		
	}
	
	protected void OnKeyReleased(KeyEvent event) {
		super.OnKeyReleased(event);
	
	
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

package org.openjfx.hellofx;

import java.util.Iterator;
import java.util.Random;

import javafx.animation.RotateTransition;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class UserViewer_Test3 extends WFXPanel2D {

	// -Dprism.order=sw

	protected void OnKeyReleased(KeyEvent event) {
		super.OnKeyReleased(event);

		if (event.getCode() == KeyCode.O) {
			groupeRectangle.setRotate(groupeRectangle.getRotate() + 10);
	//		createGrid();
		}
		if (event.getCode() == KeyCode.P) {
			groupeRectangle.setRotate(groupeRectangle.getRotate() - 10);
	//		createGrid();
		}
		if (event.getCode() == KeyCode.L) {
			groupeRectangle.setRotate(groupeRectangle.getRotate() + 1);
	//		createGrid();
		}
		if (event.getCode() == KeyCode.M) {
			groupeRectangle.setRotate(groupeRectangle.getRotate() - 1);
	//		createGrid();
		}
		System.err.println(">> "+groupeRectangle.getBoundsInLocal());
		
		debugViewPort();
		
	}

	Group groupeRectangle;

	/**
	 * Crée les objets a dessiner dans la scene
	 */
	protected void initializeObjectsToDraw() {

		// Je crée un groupe pour mes rectangle (ici je n'ai plus qu'un seul rectangle mais le bug est pareil que si j'ai une grille de 32x32)
		groupeRectangle = new Group();

		double	x			= 0;
		double	y			= 0;

		// Création du groupe de shapes associé au Flotteur
		Group	flotteurAll	= new Group();
		flotteurAll.setTranslateX(x);
		flotteurAll.setTranslateY(y);

		// Création d'une forme pour représenter visuellement le Flotteur
		Shape rect = new Rectangle(-30, -20, 60, 40);
		rect.setFill(Color.BLUE);
		rect.setStroke(Color.BLACK);
		rect.setTranslateY(10);
		
		groupeRectangle.getChildren().add(rect);
		
		rect = new Rectangle(-30, -20-30, 60, 40);
		rect.setFill(Color.BLUE);
		rect.setStroke(Color.BLACK);
		//rect.setTranslateY(-30);
		
		groupeRectangle.getChildren().add(rect);
		
		// Affiche une fleche sur le groupe entier.
		
		
		//groupeRectangle.getBoundsInLocal().getMinX()
		//groupeRectangle.getBoundsInLocal().getMaxX()
		
		//double	w		= groupeRectangle.getBoundsInLocal().getWidth();
		Group	arrow	= createArrowInWorldSpaceWithTextInScreeSpace(groupeRectangle.getBoundsInLocal().getMinX(), -50, groupeRectangle.getBoundsInLocal().getMaxX(), -50, 3, new Text("700"));
		groupeRectangle.getChildren().add(arrow);

		System.err.println(">> "+groupeRectangle.getBoundsInLocal());
		
		
		addNodeToScene(groupeRectangle);

	}

}

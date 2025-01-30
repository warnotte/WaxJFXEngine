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

public class UserViewer_Test4 extends WFXPanel2D {

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

		Group g1 = creatQuad(360);
		g1.setTranslateX(-80);
		g1.setTranslateY(-80);
		groupeRectangle.getChildren().add(g1);
		Group g2 = creatQuad(-360);
		g2.setTranslateX(80);
		g2.setTranslateY(-80);
		groupeRectangle.getChildren().add(g2);
		Group g3 = creatQuad(-360);
		g3.setTranslateX(-80);
		g3.setTranslateY(80);
		groupeRectangle.getChildren().add(g3);
		Group g4 = creatQuad(360);
		g4.setTranslateX(80);
		g4.setTranslateY(80);
		groupeRectangle.getChildren().add(g4);
		
		// Affiche une fleche sur le groupe entier.
		createRotation(groupeRectangle, -360, 25000);
		
		//groupeRectangle.getBoundsInLocal().getMinX()
		//groupeRectangle.getBoundsInLocal().getMaxX()
		
		//double	w		= groupeRectangle.getBoundsInLocal().getWidth();
		//groupeRectangle.getChildren().add(arrow);

		System.err.println(">> "+groupeRectangle.getBoundsInLocal());
		
		
		addNodeToScene(groupeRectangle);

	}

	public Group creatQuad(double angle)
	{
		Group g = new Group();
		
		
		Rectangle rect;
		
		rect = new Rectangle(-20, -20, 40, 40);
		rect.setFill(Color.BLUE);
		rect.setStroke(Color.BLACK);
		rect.setTranslateX(-30);
		rect.setTranslateY(-30);
		g.getChildren().add(rect);
		addLabelToShapeInScreenSpace("1.", rect, 0 , 10);
		createRotation(rect, -360, 5000);
		addShapeToSelectable(rect, new Flotteur("", 0,0,0));
		
		
		rect = new Rectangle(-20, -20, 40, 40);
		rect.setFill(Color.BLUE);
		rect.setStroke(Color.BLACK);
		rect.setTranslateX(30);
		rect.setTranslateY(-30);
		g.getChildren().add(rect);
		addLabelToShapeInScreenSpace("2.", rect, 0 , 10);
		addShapeToSelectable(rect, new Flotteur("", 0,0,0));
		
		rect = new Rectangle(-20, -20, 40, 40);
		rect.setFill(Color.BLUE);
		rect.setStroke(Color.BLACK);
		rect.setTranslateX(-30);
		rect.setTranslateY(30);
		g.getChildren().add(rect);
		addLabelToShapeInScreenSpace("3.", rect, 0 , 10);
		addShapeToSelectable(rect, new Flotteur("", 0,0,0));
		
		
		Group gsub4 = new Group();
		rect = new Rectangle(-20, -20, 40, 40);
		rect.setFill(Color.BLUE);
		rect.setStroke(Color.BLACK);
		gsub4.setTranslateX(30);
		gsub4.setTranslateY(30);
		addLabelToShapeInScreenSpace("4.", rect, 0 , 10);
		gsub4.getChildren().add(rect);
		addShapeToSelectable(rect, new Flotteur("", 0,0,0));
		
		// 10 - 50 
		Group	arrow	= createArrowInWorldSpaceWithTextInScreeSpace(rect.getBoundsInParent().getMinX(), 25, rect.getBoundsInParent().getMaxX(), 25, 3, new Text("ABC"));
		gsub4.getChildren().add(arrow);
		createRotation(gsub4, 360, 5000);
		
		
		g.getChildren().add(gsub4);

		Circle circle = new Circle(10);
		g.getChildren().add(circle);

		
		createRotation(g, angle, 10000);
		
		return g;
		
	}
	
	public void createRotation(Node node, double angle, double time)
	{
		//Creating a rotate transition    
	      RotateTransition rotateTransition = new RotateTransition(); 
	      
	      //Setting the duration for the transition 
	      rotateTransition.setDuration(Duration.millis(time)); 
	      
	      //Setting the node for the transition 
	      rotateTransition.setNode(node);       
	      
	      //Setting the angle of the rotation 
	      rotateTransition.setByAngle(angle); 
	      
	      //Setting the cycle count for the transition 
	      rotateTransition.setCycleCount(50); 
	      
	      //Setting auto reverse value to false 
	      rotateTransition.setAutoReverse(false); 
	      
	      //Playing the animation 
	      rotateTransition.play(); 
	}
}

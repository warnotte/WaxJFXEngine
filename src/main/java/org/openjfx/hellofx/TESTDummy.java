package org.openjfx.hellofx;
import javafx.animation.RotateTransition;
import javafx.application.Application; 
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;  

public class TESTDummy extends Application{ 
   @Override
   public void start(Stage stage) { 
  
        
	   
	// Je crée un groupe pour mes rectangle (ici je n'ai plus qu'un seul rectangle mais le bug est pareil que si j'ai une
			// grille de 32x32)
	   Group groupeRectangle = new Group();

			double x = 0;
			double y = 0;

			// Création du groupe de shapes associé au Flotteur
			Group flotteurAll = new Group();
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

			// groupeRectangle.getBoundsInLocal().getMinX()
			// groupeRectangle.getBoundsInLocal().getMaxX()

			// double w = groupeRectangle.getBoundsInLocal().getWidth();
			// groupeRectangle.getChildren().add(arrow);

			System.err.println(">> " + groupeRectangle.getBoundsInLocal());

		
      //Creating a Scene 
      Scene scene = new Scene(groupeRectangle, 600, 300); 
         
      //Setting title to the scene 
      stage.setTitle("Sample application"); 
         
      //Adding the scene to the stage 
      stage.setScene(scene); 
         
      //Displaying the contents of a scene 
      stage.show(); 
   }      
   
   
   public Group creatQuad(double angle) {
		Group g = new Group();

		Rectangle rect;

		rect = new Rectangle(-20, -20, 40, 40);
		rect.setFill(Color.BLUE);
		rect.setStroke(Color.BLACK);
		rect.setTranslateX(-30);
		rect.setTranslateY(-30);
		g.getChildren().add(rect);
		createRotation(rect, -360, 5000);
		
		rect = new Rectangle(-20, -20, 40, 40);
		rect.setFill(Color.BLUE);
		rect.setStroke(Color.BLACK);
		rect.setTranslateX(30);
		rect.setTranslateY(-30);
		g.getChildren().add(rect);

		rect = new Rectangle(-20, -20, 40, 40);
		rect.setFill(Color.BLUE);
		rect.setStroke(Color.BLACK);
		rect.setTranslateX(-30);
		rect.setTranslateY(30);
		g.getChildren().add(rect);

		Group gsub4 = new Group();
		rect = new Rectangle(-20, -20, 40, 40);
		rect.setFill(Color.BLUE);
		rect.setStroke(Color.BLACK);
		gsub4.setTranslateX(30);
		gsub4.setTranslateY(30);
		gsub4.getChildren().add(rect);
	
		// 10 - 50
	
		g.getChildren().add(gsub4);

		Circle circle = new Circle(10);
		g.getChildren().add(circle);

		createRotation(g, angle, 10000);

		return g;

	}

	public void createRotation(Node node, double angle, double time) {
		// Creating a rotate transition
		RotateTransition rotateTransition = new RotateTransition();

		// Setting the duration for the transition
		rotateTransition.setDuration(Duration.millis(time));

		// Setting the node for the transition
		rotateTransition.setNode(node);

		// Setting the angle of the rotation
		rotateTransition.setByAngle(angle);

		// Setting the cycle count for the transition
		rotateTransition.setCycleCount(50);

		// Setting auto reverse value to false
		rotateTransition.setAutoReverse(false);

		// Playing the animation
		rotateTransition.play();
	}
   
   public static void main(String args[]){ 
      launch(args); 
   } 
}       
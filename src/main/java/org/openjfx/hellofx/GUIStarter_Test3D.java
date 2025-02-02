package org.openjfx.hellofx;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

public class GUIStarter_Test3D extends Application {

	public static void main(final String[] args) {
		launch(args);
	}
	
	//Tracks drag starting point for x and y
	private double anchorX, anchorY;
	//Keep track of current angle for x and y
	private double anchorAngleX = 0;
	private double anchorAngleY = 0;
	//We will update these after drag. Using JavaFX property to bind with object
	private final DoubleProperty angleX = new SimpleDoubleProperty(0);
	private final DoubleProperty angleY = new SimpleDoubleProperty(0);
	 
	private void initMouseControl(SmartGroup group, Scene scene) {
	   Rotate xRotate;
	   Rotate yRotate;
	   group.getTransforms().addAll(
	       xRotate = new Rotate(0, Rotate.X_AXIS),
	       yRotate = new Rotate(0, Rotate.Y_AXIS)
	   );
	   xRotate.angleProperty().bind(angleX);
	   yRotate.angleProperty().bind(angleY);
	 
	   scene.setOnMousePressed(event -> {
	     anchorX = event.getSceneX();
	     anchorY = event.getSceneY();
	     anchorAngleX = angleX.get();
	     anchorAngleY = angleY.get();
	   });
	 
	   scene.setOnMouseDragged(event -> {
	     angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
	     angleY.set(anchorAngleY + anchorX - event.getSceneX());
	   });
	 }

	@Override
	public void start(Stage primaryStage) {

		Group root = new Group();

		
		//Prepare X and Y axis rotation transformation obejcts
		Rotate xRotate;
		Rotate yRotate;
		//Add both transformation to the container
		root.getTransforms().addAll(
		    xRotate = new Rotate(0, Rotate.X_AXIS),
		    yRotate = new Rotate(0, Rotate.Y_AXIS)
		);
		/*Bind Double property angleX/angleY with corresponding transformation.
		  When we update angleX / angleY, the transform will also be auto updated.*/
		xRotate.angleProperty().bind(angleX);
		yRotate.angleProperty().bind(angleY);
		
		  //Preparing the phong material of type diffuse color 
	      PhongMaterial material5 = new PhongMaterial();  
	      material5.setDiffuseColor(Color.RED); 
	      //Preparing the phong material of type diffuse color 
	      PhongMaterial material6 = new PhongMaterial();  
	      material6.setDiffuseColor(Color.BLUE); 
	      
	      for (int x = 0; x < 10; x++) {
		      for (int y = 0; y < 10; y++) {
			
		
			Group flotteur = new Group();
			// Drawing a Box
			Box box = new Box();
			// Setting the properties of the Box
			box.setWidth(100.0);
			box.setHeight(40.0);
			box.setDepth(100.0);
			box.setMaterial(material5);
			
			Box ps = new Box();
			// Setting the properties of the Box
			ps.setWidth(100.0);
			ps.setHeight(1.0);
			ps.setDepth(100.0);
			ps.setMaterial(material6);
			ps.setTranslateY(-40 - 10);
			ps.setRotate(30);

			flotteur.getChildren().add(box);
			flotteur.getChildren().add(ps);
	
			flotteur.setTranslateX(x* 110 - 1100/2);
			flotteur.setTranslateZ(y* 110 - 1100/2);
			
			root.getChildren().add(flotteur);
	      }
	      }
	      
	      
	      MeshView mesh = createWaterMesh(100, 100, 20f, 2f);
	      
	      //cubeMesh.setCullFace(CullFace.BACK);
	      mesh.setTranslateX(-20);
	      mesh.setTranslateY(0);
	      mesh.setTranslateZ(-20);
	      
	      root.getChildren().add(mesh);
	      
	      mesh.getBoundsInParent();
	      
		
	    //Move to center of the screen
		//root.translateXProperty().set(-root.getBoundsInParent().getWidth() / 2);
	  //  root.translateZProperty().set(-root.getBoundsInParent().getHeight() / 2);
	  //  root.translateYProperty().set(-1200);

		BorderPane pane = new BorderPane();
		pane.setCenter(root);

		Scene scene = new Scene(pane, 800, 600, true);

		// Setting camera
		PerspectiveCamera camera = new PerspectiveCamera(false);
		camera.setTranslateX(0);
		camera.setTranslateY(-100);
		camera.setTranslateZ(-100);
		camera.setFarClip(1000);
		scene.setCamera(camera);
		
		

		primaryStage.setTitle("3D Engine");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case W:
                    camera.translateZProperty().set(camera.getTranslateZ() + 100);
                    break;
                case S:
                    camera.translateZProperty().set(camera.getTranslateZ() - 100);
                    break;
            }
        });
		
		//Listen for mouse press -- Drag start with a click :-)
		scene.setOnMousePressed(event -> {
		  //Save start points
		  anchorX = event.getSceneX();
		  anchorY = event.getSceneY();
		  //Save current rotation angle
		  anchorAngleX = angleX.get();
		  anchorAngleY = angleY.get();
		});

		//Listen for drag
		scene.setOnMouseDragged(event -> {
		    /*event.getSceneY() gives current Y value. Find how much far away
		      it is from saved anchorY point.
		     */
		    angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
		    angleY.set(anchorAngleY + anchorX - event.getSceneX());
		});
		// ScenicView.show(scene);

		//Attach a scroll listener
		primaryStage.addEventHandler(ScrollEvent.SCROLL, event -> {
		        //Get how much scroll was done in Y axis.
		    double delta = -event.getDeltaY();
		        //Add it to the Z-axis location.
		    root.translateZProperty().set(root.getTranslateZ() + delta);
		});
	}
	
	class SmartGroup extends Group {
	    Rotate r;
	    Transform t = new Rotate();
	 
	    void rotateByX(int ang) {
	      r = new Rotate(ang, Rotate.X_AXIS);
	      t = t.createConcatenation(r);
	      this.getTransforms().clear();
	      this.getTransforms().addAll(t);
	    }
	 
	    void rotateByY(int ang) {
	      r = new Rotate(ang, Rotate.Y_AXIS);
	      t = t.createConcatenation(r);
	      this.getTransforms().clear();
	      this.getTransforms().addAll(t);
	    }
	  }
	
	  public static MeshView createWaterMesh(int width, int depth, float waveHeight, float frequency) {
	        TriangleMesh mesh = new TriangleMesh();

	        int cols = width;
	        int rows = depth;
	        float cellSize = 10.0f; // Taille de chaque cellule

	        // Ajout des points
	        for (int z = 0; z < rows; z++) {
	            for (int x = 0; x < cols; x++) {
	                float y = (float) (waveHeight * Math.sin(x * frequency) * Math.cos(z * frequency));
	                mesh.getPoints().addAll(x * cellSize, y, z * cellSize);
	            }
	        }

	        // Définition des faces par index
	        for (int z = 0; z < rows - 1; z++) {
	            for (int x = 0; x < cols - 1; x++) {
	                int topLeft = z * cols + x;
	                int topRight = topLeft + 1;
	                int bottomLeft = (z + 1) * cols + x;
	                int bottomRight = bottomLeft + 1;

	                // Première face du carré
	                mesh.getFaces().addAll(
	                    topLeft, 0, bottomLeft, 0, topRight, 0, // Indices
	                    topRight, 0, bottomLeft, 0, bottomRight, 0
	                );
	            }
	        }

	        return new MeshView(mesh);
	    }
}
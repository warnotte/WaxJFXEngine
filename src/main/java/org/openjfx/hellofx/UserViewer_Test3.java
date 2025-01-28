package org.openjfx.hellofx;

import java.util.Iterator;
import java.util.Random;

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
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

public class UserViewer_Test3 extends WFXPanel2D {
	
	//-Dprism.order=sw
	
	protected void OnKeyReleased(KeyEvent event) {
		super.OnKeyReleased(event);
	
		if (event.getCode() == KeyCode.G) {
			System.out.println("Touche G appuyée.");
			moveSelectedFlotteursRandomly();
		}
	
		if (event.getCode() == KeyCode.O) {
			groupeRectangle.setRotate(groupeRectangle.getRotate()+1);
			//createGrid();
			//reinitializeScene();
		}
		if (event.getCode() == KeyCode.P) {
			groupeRectangle.setRotate(groupeRectangle.getRotate()-1);
			//createGrid();
			//reinitializeScene();
		}
		
	}
	
	
    

	private void createModel() {
		model = new Flotteur[32][32];;
		   for (int i = 0; i < model.length; i++) {
	           for (int j = 0; j < model[i].length; j++) {
	        	   

	        	   // Création de l'objet métier Flotteur avec position et rotation
	        	   double x = j * 100;
	        	   double y = i * 100;
	        	   double rotation = 0;//(j + i) * 10; // Exemple de rotation
	        	   Flotteur flotteur = new Flotteur("Flotteur " + j + "_" + i, x, y, rotation);
	        	   model[i][j]=flotteur;
	           }
		   }
	}

    private void moveSelectedFlotteursRandomly() {
        Random random = new Random();

        // Parcourir les objets sélectionnés
        for (Flotteur flotteur : getSelectedObjects(Flotteur.class)) {
            // Modifier la position du Flotteur de manière aléatoire
            double deltaX = random.nextDouble() * 100 - 50; // Valeur entre -5 et +5
            double deltaY = random.nextDouble() * 100 - 50;
            flotteur.setX(flotteur.getX() + deltaX);
            flotteur.setY(flotteur.getY() + deltaY);
        }
        // Mettre à jour le groupe visuel correspondant
        reinitializeScene();
    }


	

	Group groupeRectangle;

	// MON MODEL
	Flotteur [][] model = null;
	
	/**
	 * Crée les objets a dessiner dans la scene
	 */
	protected void initializeObjectsToDraw() {

		if (model==null)
			createModel();
		
	    // Ici je vais dessiner ma scene avec des shape et associer un objet "metier"
		groupeRectangle = new Group();
		//Transform e = Transform.translate(-450, -450);
	    //groupeRectangle.getTransforms().add(e);
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
    			/*if (Math.random()>=0.7)
    			{
    	        RotateTransition transition2 = new RotateTransition(Duration.seconds(15), flotteurAll);
    	        transition2.setFromAngle(0);
    	        transition2.setToAngle(360);
    	        transition2.setInterpolator(Interpolator.LINEAR);
    	        transition2.setCycleCount(0);
    	        transition2.play();
    			}*/
    			

               	// Création d'une forme pour représenter visuellement le Flotteur
               	int rnd = 0;//i%9;
               	Shape rect = createShapeForFlotteur(rnd);

             	rect.setFill(Color.BLUE);
    			rect.setStroke(Color.BLACK);
    			// rect.setStrokeType(StrokeType.CENTERED); // Attention avec les lignes ...
    			
    			rect.setOnMouseEntered(event -> {
    			//	System.err.println("Entered");
    			});
    			rect.setOnMouseExited(event -> {
    			//	System.err.println("Exited");
    			});
    			rect.setOnMouseClicked(event -> {
    			//	System.err.println("Clicked");
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
    				Group arrow = createArrowInWorldSpaceWithTextInScreeSpace(-30, -20-1, 30, -20-1, 3, new Text("L"));
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
        double h = groupeRectangle.getBoundsInLocal().getHeight();
        Group arrow = createArrowInWorldSpaceWithTextInScreeSpace(0, -100, w, -100, 3, new Text("700"));
        
       // groupeRectangle.setRotate(45);
	   // groupeRectangle.setTranslateX(-3268/2);
       // groupeRectangle.setTranslateY(-3268/2);
    
        
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

	
}

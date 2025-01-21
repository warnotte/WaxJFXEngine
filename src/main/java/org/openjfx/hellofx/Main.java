package org.openjfx.hellofx; 
  
import javafx.application.Application; 
import javafx.scene.Scene; 
import javafx.scene.control.Button; 
import javafx.scene.control.ToolBar; 
import javafx.scene.layout.BorderPane; 
import javafx.scene.layout.Pane; 
import javafx.scene.layout.Region; 
import javafx.scene.shape.Circle; 
import javafx.scene.shape.Rectangle; 
import javafx.stage.Stage; 
  
import java.net.URL; 
import java.util.Optional; 
  
public final class Main extends Application { 
    public static void main(final String... args) { 
        launch(args); 
    } 
  
  
    @Override 
    public void start(final Stage stage) throws Exception { 
        // Toolbar & button. 
        final var button = new Button("Button"); 
        final var tooBar = new ToolBar(); 
        tooBar.getItems().setAll(button); 
        // Shapes & region. 
        final var rectangle = new Rectangle(200, 200, 150, 100); 
        rectangle.getStyleClass().add("rectangle"); 
        final var circle = new Circle(100); 
        circle.getStyleClass().add("circle"); 
        circle.setCenterX(150); 
        circle.setCenterY(150); 
        final var region = new Region(); 
        region.getStyleClass().add("region"); 
        region.setLayoutX(300); 
        region.setLayoutY(275); 
        final var center = new Pane(); 
        center.getChildren().setAll(rectangle, circle, region); 
        // Layout. 
        final var root = new BorderPane(); 
        root.setTop(tooBar); 
        root.setCenter(center); 
        final var scene = new Scene(root); 
        // load and apply CSS. 
        Optional.ofNullable(getClass().getResource("/test.css")) 
                .map(URL::toExternalForm) 
                .ifPresent(scene.getStylesheets()::add); 
        stage.setTitle("Test"); 
        stage.setWidth(800); 
        stage.setHeight(600); 
        stage.setScene(scene); 
        stage.show(); 
    } 
}
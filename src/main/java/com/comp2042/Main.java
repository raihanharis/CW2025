package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/gameLayout.fxml")
        );

        Parent root = loader.load();

        // Get the GUI controller from FXML
        GuiController gui = loader.getController();

        // Create the game controller and connect it to GUI
        new GameController(gui);

        primaryStage.setTitle("Tetris");
        Scene scene = new Scene(root, 450, 580);
        scene.setFill(javafx.scene.paint.Color.BLACK);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(400);
        primaryStage.show();
        
        // Request focus for keyboard input
        root.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

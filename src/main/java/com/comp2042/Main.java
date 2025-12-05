package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the main menu first
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/mainMenu.fxml")
        );

        Parent root = loader.load();

        // Get the MainMenuController and set the primary stage
        MainMenuController mainMenuController = loader.getController();
        mainMenuController.setPrimaryStage(primaryStage);

        // Create and show the main menu scene
        primaryStage.setTitle("Tetris");
        Scene scene = new Scene(root, 900, 700);
        scene.setFill(javafx.scene.paint.Color.web("#000000"));
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(550);
        primaryStage.show();
        
        // Request focus for keyboard input
        root.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

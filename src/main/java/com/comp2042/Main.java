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

        primaryStage.setTitle("My Tetris Game");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

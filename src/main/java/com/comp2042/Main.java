package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load arcade fonts globally before creating any UI
        loadArcadeFonts();
        
        // Load the main menu first
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/mainMenu.fxml")
        );

        Parent root = loader.load();

        // Get the MainMenuController (no need to set primaryStage - controllers get it from scene)
        MainMenuController mainMenuController = loader.getController();

        // Create and show the main menu scene
        primaryStage.setTitle("Tetris");
        Scene scene = new Scene(root, 900, 700);
        scene.setFill(javafx.scene.paint.Color.web("#000000"));
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(550);
        
        // Ensure the root is interactive and not blocking events
        root.setMouseTransparent(false);
        root.setDisable(false);
        root.setPickOnBounds(true);
        
        // Show stage
        primaryStage.show();
        
        // Request focus for keyboard input
        javafx.application.Platform.runLater(() -> {
            root.requestFocus();
        });
    }
    
    /**
     * Loads arcade fonts globally for the entire application.
     * Tries to load fonts from the fonts/ directory.
     */
    private void loadArcadeFonts() {
        try {
            // Try to load Press Start 2P font
            try {
                Font.loadFont(getClass().getResourceAsStream("/fonts/press-start-2p.ttf"), 16);
                System.out.println("Loaded: Press Start 2P");
            } catch (Exception e) {
                // Font not found, try alternatives
            }
            
            // Try to load VT323 font
            try {
                Font.loadFont(getClass().getResourceAsStream("/fonts/vt323.ttf"), 16);
                System.out.println("Loaded: VT323");
            } catch (Exception e) {
                // Font not found
            }
            
            // Try to load ArcadeClassic font
            try {
                Font.loadFont(getClass().getResourceAsStream("/fonts/arcade-classic.ttf"), 16);
                System.out.println("Loaded: ArcadeClassic");
            } catch (Exception e) {
                // Font not found
            }
            
            // Fallback: Load digital.ttf if available
            try {
                Font.loadFont(getClass().getResourceAsStream("/digital.ttf"), 16);
                System.out.println("Loaded: digital.ttf (fallback)");
            } catch (Exception e) {
                // Font not found
            }
        } catch (Exception e) {
            System.err.println("Error loading fonts: " + e.getMessage());
            // Continue without custom fonts - will use system fonts
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

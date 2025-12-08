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
        // Load arcade fonts globally before creating any UI (heavy initialization at startup)
        loadArcadeFonts();
        
        // PRELOAD ALL FXML root nodes at startup to prevent flicker
        SceneManager.preloadAllScenes();
        
        // Register the primary stage with StageManager
        StageManager.setPrimaryStage(primaryStage);
        
        // Get the preloaded main menu root
        Parent mainMenuRoot = SceneManager.getPreloadedRoot("mainMenu");
        if (mainMenuRoot == null) {
            System.err.println("ERROR: Main menu root not preloaded!");
            return;
        }
        
        // Configure root
        mainMenuRoot.setMouseTransparent(false);
        mainMenuRoot.setDisable(false);
        mainMenuRoot.setPickOnBounds(true);
        
        // Create SINGLE Scene that will be used for entire application
        // This Scene is NEVER replaced - only its root node is swapped
        Scene singleScene = SceneManager.createSingleScene(mainMenuRoot);
        
        // Set up the stage - DO NOT modify size, position, or state
        primaryStage.setTitle("Tetris");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(550);
        
        // Set the SINGLE scene - this is the ONLY scene assignment ever
        primaryStage.setScene(singleScene);
        
        // Show stage ONCE - never call show() again after this
        primaryStage.show();
        
        // Request focus for keyboard input
        javafx.application.Platform.runLater(() -> {
            mainMenuRoot.requestFocus();
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

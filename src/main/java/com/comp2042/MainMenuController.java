package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Main Menu scene.
 * Handles navigation between main menu, game, and settings.
 */
public class MainMenuController implements Initializable {

    @FXML
    private Button startGameButton;
    
    @FXML
    private Button settingsButton;
    
    @FXML
    private Button exitButton;
    
    @FXML
    private javafx.scene.control.Label tetrisTitle;
    
    private Stage primaryStage;
    
    // Scale transitions for hover and press effects (subtle animations only)
    // No pulse animations - styling handled via CSS
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load arcade fonts for main menu
        loadArcadeFonts();
        
        // Setup subtle hover/press animations (scale and translateY only)
        setupButtonAnimations();
    }
    
    /**
     * Loads arcade-style fonts for the main menu.
     * Tries to load PressStart2P, VT323, ArcadeClassic, or digital.ttf from resources.
     */
    private void loadArcadeFonts() {
        boolean fontLoaded = false;
        
        // Try to load PressStart2P.ttf from fonts directory (more animated/playful)
        try {
            java.io.InputStream fontStream = getClass().getResourceAsStream("/fonts/PressStart2P.ttf");
            if (fontStream != null) {
                javafx.scene.text.Font.loadFont(fontStream, 22);
                fontLoaded = true;
                System.out.println("Loaded: PressStart2P.ttf from fonts/ directory");
            }
        } catch (Exception e) {
            // Font not found
        }
        
        // Try VT323 (pixel/terminal style - more animated)
        if (!fontLoaded) {
            try {
                java.io.InputStream fontStream = getClass().getResourceAsStream("/fonts/VT323.ttf");
                if (fontStream != null) {
                    javafx.scene.text.Font.loadFont(fontStream, 22);
                    fontLoaded = true;
                    System.out.println("Loaded: VT323.ttf from fonts/ directory");
                }
            } catch (Exception e) {
                // Font not found
            }
        }
        
        // Try ArcadeClassic.ttf from fonts directory
        if (!fontLoaded) {
            try {
                java.io.InputStream fontStream = getClass().getResourceAsStream("/fonts/ArcadeClassic.ttf");
                if (fontStream != null) {
                    javafx.scene.text.Font.loadFont(fontStream, 22);
                    fontLoaded = true;
                    System.out.println("Loaded: ArcadeClassic.ttf from fonts/ directory");
                }
            } catch (Exception e) {
                // Font not found
            }
        }
        
        // Fallback: Load digital.ttf if available
        if (!fontLoaded) {
            try {
                javafx.scene.text.Font.loadFont(
                        getClass().getClassLoader().getResource("digital.ttf").toExternalForm(),
                        22
                );
                fontLoaded = true;
                System.out.println("Loaded: digital.ttf (fallback)");
            } catch (Exception e) {
                System.out.println("Using system fonts as fallback");
            }
        }
    }
    
    /**
     * Sets up subtle button animations: hover scale (1.03x) with translateY (2-3px), press scale (0.98x).
     * All animations are subtle and use CSS transitions (150-200ms).
     */
    private void setupButtonAnimations() {
        // Setup subtle hover animations (1.00 → 1.03x scale, 2-3px translateY, 175ms)
        setupHoverScale(startGameButton);
        setupHoverScale(settingsButton);
        setupHoverScale(exitButton);
        
        // Setup press scale animations (1.00 → 0.98x, 150ms)
        setupPressScale(startGameButton);
        setupPressScale(settingsButton);
        setupPressScale(exitButton);
    }
    
    /**
     * REMOVED: Continuous text animation - no longer needed.
     * Text styling is now handled entirely via CSS.
     */
    
    /**
     * Sets up subtle hover animations: scale (1.03x) and vertical lift (2-3px translateY).
     * Smooth 175ms ease-out transition.
     */
    private void setupHoverScale(Button button) {
        if (button == null) return;
        
        // Button scale animation (1.00 → 1.03, 175ms ease-out) - subtle hover scale
        ScaleTransition scale = new ScaleTransition(Duration.millis(175), button);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.03);
        scale.setToY(1.03);
        scale.setInterpolator(javafx.animation.Interpolator.EASE_OUT);  // Smooth ease-out transition
        
        // Vertical lift animation (175ms) - subtle 2-3px upward movement
        TranslateTransition translate = new TranslateTransition(Duration.millis(175), button);
        translate.setFromY(0);
        translate.setToY(-2.5);  // Slide upward by 2.5px (subtle lift)
        translate.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        button.setOnMouseEntered(e -> {
            // Subtle hover effects: scale up and lift
            scale.play();
            translate.play();
        });
        
        button.setOnMouseExited(e -> {
            // Reverse scale animation (175ms ease-out)
            ScaleTransition reverseScale = new ScaleTransition(Duration.millis(175), button);
            reverseScale.setFromX(button.getScaleX());
            reverseScale.setFromY(button.getScaleY());
            reverseScale.setToX(1.0);
            reverseScale.setToY(1.0);
            reverseScale.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            reverseScale.play();
            
            // Reverse translate animation (175ms)
            TranslateTransition reverseTranslate = new TranslateTransition(Duration.millis(175), button);
            reverseTranslate.setFromY(button.getTranslateY());
            reverseTranslate.setToY(0);
            reverseTranslate.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            reverseTranslate.play();
        });
    }
    
    /**
     * Sets up subtle press scale animation (button shrinks to 0.98x, 150ms).
     * CSS handles the background color change.
     */
    private void setupPressScale(Button button) {
        if (button == null) return;
        
        ScaleTransition pressScale = new ScaleTransition(Duration.millis(150), button);
        pressScale.setFromX(1.0);
        pressScale.setFromY(1.0);
        pressScale.setToX(0.98);  // Subtle shrink to 0.98x
        pressScale.setToY(0.98);
        pressScale.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        button.setOnMousePressed(e -> {
            pressScale.play();
        });
        
        button.setOnMouseReleased(e -> {
            // Bounce back to normal size (150ms ease-out)
            ScaleTransition release = new ScaleTransition(Duration.millis(150), button);
            release.setFromX(button.getScaleX());
            release.setFromY(button.getScaleY());
            release.setToX(1.0);
            release.setToY(1.0);
            release.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            release.play();
        });
    }
    
    /**
     * REMOVED: Entry animations and pulse animations - no longer needed.
     * Buttons are always visible and styling is handled via CSS.
     * Title is static with soft glow via CSS.
     */
    
    /**
     * Sets the primary stage for scene switching.
     * Called from Main.java after loading the FXML.
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    /**
     * Starts a new game by loading the gameLayout.fxml scene.
     */
    @FXML
    private void startGame(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gameLayout.fxml")
            );
            
            Parent gameRoot = loader.load();
            
            // Get the GUI controller from FXML
            GuiController gui = loader.getController();
            
            // Create the game controller and connect it to GUI
            new GameController(gui);
            
            // Set the primary stage reference in GuiController for scene switching
            gui.setPrimaryStage(primaryStage);
            
            // Update Hard Drop label visibility based on current settings
            gui.updateHardDropLabelVisibility();
            
            // Create and set the game scene
            Scene gameScene = new Scene(gameRoot, 900, 700);
            gameScene.setFill(javafx.scene.paint.Color.web("#000000"));
            
            if (primaryStage != null) {
                // Set fullscreen BEFORE scene change to prevent exit
                primaryStage.setFullScreen(true);
                primaryStage.setFullScreenExitHint("");
                primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
                
                primaryStage.setScene(gameScene);
                primaryStage.setTitle("Tetris - Game");
                
                // Force fullscreen immediately after scene change
                javafx.application.Platform.runLater(() -> {
                    primaryStage.setFullScreen(true);
                    primaryStage.setFullScreenExitHint("");
                    primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
                });
                
                // Request focus for keyboard input
                gameRoot.requestFocus();
            }
            
        } catch (IOException e) {
            System.err.println("Error loading game scene: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Opens the settings scene.
     */
    @FXML
    private void openSettings(ActionEvent event) {
        System.out.println("\n\n========================================");
        System.out.println(">>> SETTINGS BUTTON CLICKED! <<<");
        System.out.println("========================================\n");
        
        if (primaryStage == null) {
            System.err.println("ERROR: primaryStage is null!");
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot Open Settings");
            alert.setContentText("Primary stage is not set. Please restart the application.");
            alert.showAndWait();
            return;
        }
        
        try {
            // Load FXML
            java.net.URL settingsUrl = getClass().getResource("/settings.fxml");
            if (settingsUrl == null) {
                System.err.println("ERROR: Cannot find /settings.fxml");
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Cannot Load Settings");
                alert.setContentText("Settings FXML file not found!");
                alert.showAndWait();
                return;
            }
            System.out.println("Found settings.fxml");
            
            FXMLLoader loader = new FXMLLoader(settingsUrl);
            System.out.println("Loading FXML...");
            Parent settingsRoot = loader.load();
            System.out.println("FXML loaded!");
            
            // Get controller
            SettingsController controller = loader.getController();
            if (controller != null) {
                controller.setPrimaryStage(primaryStage);
                System.out.println("Controller initialized");
            } else {
                System.err.println("WARNING: Controller is null");
            }
            
            // Create scene
            Scene settingsScene = new Scene(settingsRoot, 900, 700);
            settingsScene.setFill(javafx.scene.paint.Color.web("#000000"));
            System.out.println("Scene created");
            
            // Set scene on stage
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
            
            primaryStage.setScene(settingsScene);
            primaryStage.setTitle("Tetris - Settings");
            System.out.println("Scene set on stage!");
            
            // Re-enable fullscreen after scene change
            javafx.application.Platform.runLater(() -> {
                primaryStage.setFullScreen(true);
                primaryStage.setFullScreenExitHint("");
                primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
            });
            
            settingsRoot.requestFocus();
            System.out.println("Settings screen should be visible now!\n");
            
        } catch (javafx.fxml.LoadException e) {
            System.err.println("FXML Load Exception:");
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("FXML Error");
            alert.setHeaderText("Cannot Load Settings Screen");
            alert.setContentText("Error loading settings.fxml:\n" + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Unexpected error:");
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot Open Settings");
            alert.setContentText("An error occurred:\n" + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Exits the application immediately without confirmation.
     */
    @FXML
    private void exitGame(ActionEvent event) {
        if (primaryStage != null) {
            primaryStage.close();
        } else {
            // Fallback: exit the JavaFX application
            javafx.application.Platform.exit();
        }
    }
}


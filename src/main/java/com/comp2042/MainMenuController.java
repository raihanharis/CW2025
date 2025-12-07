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
    
    @FXML
    private javafx.scene.layout.VBox mainMenuRoot;
    
    private Stage primaryStage;
    
    // Scale transitions for hover and press effects (subtle animations only)
    // No pulse animations - styling handled via CSS
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("MainMenuController.initialize() called");
            
            // Load arcade fonts for main menu (safe - won't break if fonts missing)
            try {
                loadArcadeFonts();
            } catch (Exception e) {
                System.err.println("WARNING: Could not load arcade fonts: " + e.getMessage());
                // Continue without custom fonts
            }
            
            // Setup subtle hover/press animations (safe - won't break if buttons null)
            try {
                setupButtonAnimations();
            } catch (Exception e) {
                System.err.println("WARNING: Could not setup button animations: " + e.getMessage());
                // Continue without animations
            }
            
            // Explicitly ensure all buttons are enabled and clickable
            // Each button setup is wrapped in try-catch to prevent one failure from breaking others
            if (startGameButton != null) {
                try {
                    startGameButton.setDisable(false);
                    startGameButton.setMouseTransparent(false);
                    startGameButton.setFocusTraversable(true);
                    startGameButton.setPickOnBounds(true);
                    System.out.println("Start Game button initialized and enabled");
                } catch (Exception e) {
                    System.err.println("WARNING: Error setting up startGameButton: " + e.getMessage());
                }
            } else {
                System.err.println("ERROR: startGameButton is null!");
            }
            
            if (settingsButton != null) {
                try {
                    settingsButton.setDisable(false);
                    settingsButton.setMouseTransparent(false);
                    settingsButton.setFocusTraversable(true);
                    settingsButton.setPickOnBounds(true);
                    System.out.println("Settings button initialized and enabled");
                } catch (Exception e) {
                    System.err.println("WARNING: Error setting up settingsButton: " + e.getMessage());
                }
            } else {
                System.err.println("ERROR: settingsButton is null!");
            }
            
            if (exitButton != null) {
                try {
                    exitButton.setDisable(false);
                    exitButton.setMouseTransparent(false);
                    exitButton.setFocusTraversable(true);
                    exitButton.setPickOnBounds(true);
                    System.out.println("Exit button initialized and enabled");
                } catch (Exception e) {
                    System.err.println("WARNING: Error setting up exitButton: " + e.getMessage());
                }
            } else {
                System.err.println("ERROR: exitButton is null!");
            }
            
            // Ensure main menu root doesn't block events
            if (mainMenuRoot != null) {
                mainMenuRoot.setMouseTransparent(false);
                mainMenuRoot.setDisable(false);
                mainMenuRoot.setPickOnBounds(true);
            }
            
            // Explicitly wire up button handlers as a fallback (in case FXML wiring fails)
            // Use both setOnAction AND addEventHandler to ensure clicks are captured
            if (startGameButton != null) {
                // Clear any existing handlers first
                startGameButton.setOnAction(null);
                startGameButton.setOnMouseClicked(null);
                
                // Set up ActionEvent handler
                startGameButton.setOnAction(e -> {
                    System.out.println(">>> START GAME ACTION EVENT FIRED! <<<");
                    startGame(e);
                });
                
                // Also add mouse click handler as backup (fires even if ActionEvent doesn't)
                startGameButton.setOnMouseClicked(e -> {
                    System.out.println(">>> START GAME MOUSE CLICKED! (x=" + e.getX() + ", y=" + e.getY() + ") <<<");
                    if (!e.isConsumed()) {
                        e.consume();
                        startGame(new ActionEvent(startGameButton, null));
                    }
                });
                
                // Add diagnostic: check if button is actually visible and enabled
                System.out.println("Start Game button state: visible=" + startGameButton.isVisible() + 
                                 ", disabled=" + startGameButton.isDisabled() + 
                                 ", mouseTransparent=" + startGameButton.isMouseTransparent() +
                                 ", managed=" + startGameButton.isManaged());
                System.out.println("Start Game button handler wired (both ActionEvent and MouseEvent)");
            }
            if (settingsButton != null) {
                // Clear any existing handlers first
                settingsButton.setOnAction(null);
                settingsButton.setOnMouseClicked(null);
                
                // Set up ActionEvent handler
                settingsButton.setOnAction(e -> {
                    System.out.println(">>> SETTINGS ACTION EVENT FIRED! <<<");
                    openSettings(e);
                });
                
                // Also add mouse click handler as backup
                settingsButton.setOnMouseClicked(e -> {
                    System.out.println(">>> SETTINGS MOUSE CLICKED! (x=" + e.getX() + ", y=" + e.getY() + ") <<<");
                    if (!e.isConsumed()) {
                        e.consume();
                        openSettings(new ActionEvent(settingsButton, null));
                    }
                });
                
                System.out.println("Settings button state: visible=" + settingsButton.isVisible() + 
                                 ", disabled=" + settingsButton.isDisabled() + 
                                 ", mouseTransparent=" + settingsButton.isMouseTransparent());
                System.out.println("Settings button handler wired (both ActionEvent and MouseEvent)");
            }
            if (exitButton != null) {
                // Clear any existing handlers first
                exitButton.setOnAction(null);
                exitButton.setOnMouseClicked(null);
                
                // Set up ActionEvent handler
                exitButton.setOnAction(e -> {
                    System.out.println(">>> EXIT ACTION EVENT FIRED! <<<");
                    exitGame(e);
                });
                
                // Also add mouse click handler as backup
                exitButton.setOnMouseClicked(e -> {
                    System.out.println(">>> EXIT MOUSE CLICKED! (x=" + e.getX() + ", y=" + e.getY() + ") <<<");
                    if (!e.isConsumed()) {
                        e.consume();
                        exitGame(new ActionEvent(exitButton, null));
                    }
                });
                
                System.out.println("Exit button state: visible=" + exitButton.isVisible() + 
                                 ", disabled=" + exitButton.isDisabled() + 
                                 ", mouseTransparent=" + exitButton.isMouseTransparent());
                System.out.println("Exit button handler wired (both ActionEvent and MouseEvent)");
            }
            
            System.out.println("MainMenuController.initialize() completed successfully!");
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("CRITICAL ERROR in MainMenuController.initialize():");
            System.err.println("========================================");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");
            // Don't rethrow - let the UI continue even if initialization partially fails
        }
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
        
        // Ensure button is enabled and clickable before setting up animations
        button.setDisable(false);
        button.setMouseTransparent(false);
        
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
            // Don't consume event - let it pass through
            scale.play();
            translate.play();
        });
        
        button.setOnMouseExited(e -> {
            // Reverse scale animation (175ms ease-out)
            // Don't consume event - let it pass through
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
        
        // Ensure button is enabled and clickable
        button.setDisable(false);
        button.setMouseTransparent(false);
        
        ScaleTransition pressScale = new ScaleTransition(Duration.millis(150), button);
        pressScale.setFromX(1.0);
        pressScale.setFromY(1.0);
        pressScale.setToX(0.98);  // Subtle shrink to 0.98x
        pressScale.setToY(0.98);
        pressScale.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        button.setOnMousePressed(e -> {
            // Don't consume event - let ActionEvent fire
            pressScale.play();
        });
        
        button.setOnMouseReleased(e -> {
            // Don't consume event - let ActionEvent fire
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
        if (event != null) {
            event.consume(); // Consume the event to prevent double-firing
        }
        System.out.println("========================================");
        System.out.println(">>> START GAME BUTTON CLICKED! <<<");
        System.out.println("========================================");
        System.out.println("Event source: " + (event != null ? event.getSource() : "null"));
        System.out.println("Event target: " + (event != null ? event.getTarget() : "null"));
        
        try {
            System.out.println("Loading gameLayout.fxml...");
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gameLayout.fxml")
            );
            
            if (loader.getLocation() == null) {
                System.err.println("ERROR: Cannot find /gameLayout.fxml");
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Cannot Load Game");
                alert.setContentText("Game layout file not found!");
                alert.showAndWait();
                return;
            }
            
            System.out.println("Found gameLayout.fxml, loading...");
            Parent gameRoot = loader.load();
            System.out.println("FXML loaded successfully!");
            
            // Get the GUI controller from FXML
            GuiController gui = loader.getController();
            if (gui == null) {
                System.err.println("ERROR: GuiController is null!");
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Cannot Start Game");
                alert.setContentText("Game controller could not be initialized.");
                alert.showAndWait();
                return;
            }
            System.out.println("GuiController obtained: " + gui);
            
            // Set the primary stage reference in GuiController FIRST (before GameController)
            if (primaryStage != null) {
                gui.setPrimaryStage(primaryStage);
                System.out.println("Primary stage set in GuiController");
            } else {
                System.err.println("ERROR: primaryStage is null in startGame!");
                return;
            }
            
            
            // Create the game controller and connect it to GUI
            System.out.println("Creating GameController...");
            try {
                new GameController(gui);
                System.out.println("GameController created successfully!");
            } catch (Exception e) {
                System.err.println("ERROR: Failed to create GameController: " + e.getMessage());
                e.printStackTrace();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Cannot Start Game");
                alert.setContentText("Failed to initialize game: " + e.getMessage());
                alert.showAndWait();
                return;
            }
            
            // Update Hard Drop label visibility based on current settings
            try {
                gui.updateHardDropLabelVisibility();
                System.out.println("Hard drop label visibility updated");
            } catch (Exception e) {
                System.err.println("WARNING: Could not update hard drop label visibility: " + e.getMessage());
                // Continue - this is not critical
            }
            
            // Create and set the game scene
            System.out.println("Creating game scene...");
            Scene gameScene = new Scene(gameRoot, 900, 700);
            gameScene.setFill(javafx.scene.paint.Color.web("#000000"));
            System.out.println("Game scene created!");
            
            // Ensure the game root is interactive and visible
            gameRoot.setMouseTransparent(false);
            gameRoot.setDisable(false);
            gameRoot.setVisible(true);
            gameRoot.setOpacity(1.0);
            
            if (primaryStage == null) {
                System.err.println("ERROR: primaryStage is null in startGame!");
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Cannot Start Game");
                alert.setContentText("Primary stage is not set.");
                alert.showAndWait();
                return;
            }
            
            System.out.println("Setting scene on primary stage...");
            
            try {
                // TEMPORARILY disable fullscreen to test if that's the issue
                boolean wasFullScreen = primaryStage.isFullScreen();
                if (wasFullScreen) {
                    primaryStage.setFullScreen(false);
                    System.out.println("Temporarily disabled fullscreen for scene change");
                }
                
                // Set scene
                primaryStage.setScene(gameScene);
                primaryStage.setTitle("Tetris - Game");
                
                // Show the stage
                primaryStage.show();
                System.out.println("Stage shown");
                
                // Request focus and re-enable fullscreen after a short delay
                javafx.application.Platform.runLater(() -> {
                    try {
                        gameRoot.requestFocus();
                        System.out.println("Game root requested focus");
                        
                        // Re-enable fullscreen after scene is rendered
                        javafx.application.Platform.runLater(() -> {
                            try {
                                if (wasFullScreen && primaryStage != null) {
                                    primaryStage.setFullScreen(true);
                                    primaryStage.setFullScreenExitHint("");
                                    primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
                                    System.out.println("Fullscreen re-enabled");
                                }
                            } catch (Exception e) {
                                System.err.println("WARNING: Error re-enabling fullscreen: " + e.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        System.err.println("WARNING: Error requesting focus: " + e.getMessage());
                    }
                });
                
                System.out.println("Scene set on stage successfully!");
                System.out.println("Game should be visible now!");
            } catch (Exception e) {
                System.err.println("ERROR: Failed to set scene: " + e.getMessage());
                e.printStackTrace();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Cannot Start Game");
                alert.setContentText("Failed to switch to game scene: " + e.getMessage());
                alert.showAndWait();
            }
            
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("CRITICAL ERROR in startGame():");
            System.err.println("========================================");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");
            
            // Show error dialog to user
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot Start Game");
            alert.setContentText("An error occurred while loading the game:\n" + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Opens the settings scene.
     */
    @FXML
    private void openSettings(ActionEvent event) {
        if (event != null) {
            event.consume(); // Consume the event to prevent double-firing
        }
        System.out.println("\n\n========================================");
        System.out.println(">>> SETTINGS BUTTON CLICKED! <<<");
        System.out.println("========================================\n");
        System.out.println("Event source: " + (event != null ? event.getSource() : "null"));
        System.out.println("Event target: " + (event != null ? event.getTarget() : "null"));
        
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
            
            // TEMPORARILY disable fullscreen to test if that's the issue
            boolean wasFullScreen = primaryStage.isFullScreen();
            if (wasFullScreen) {
                primaryStage.setFullScreen(false);
                System.out.println("Temporarily disabled fullscreen for scene change");
            }
            
            // Set scene on stage FIRST
            primaryStage.setScene(settingsScene);
            primaryStage.setTitle("Tetris - Settings");
            System.out.println("Scene set on stage!");
            
            // Ensure the settings root is visible and interactive
            settingsRoot.setVisible(true);
            settingsRoot.setOpacity(1.0);
            settingsRoot.setMouseTransparent(false);
            settingsRoot.setDisable(false);
            
            // Show the stage
            primaryStage.show();
            System.out.println("Stage shown");
            
            // Re-enable fullscreen after scene change
            javafx.application.Platform.runLater(() -> {
                settingsRoot.requestFocus();
                System.out.println("Settings root requested focus");
                
                // Re-enable fullscreen after scene is rendered
                javafx.application.Platform.runLater(() -> {
                    if (wasFullScreen) {
                        primaryStage.setFullScreen(true);
                        primaryStage.setFullScreenExitHint("");
                        primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
                        System.out.println("Fullscreen re-enabled");
                    }
                });
            });
            
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
        if (event != null) {
            event.consume(); // Consume the event to prevent double-firing
        }
        System.out.println(">>> EXIT BUTTON CLICKED! <<<");
        if (primaryStage != null) {
            primaryStage.close();
        } else {
            // Fallback: exit the JavaFX application
            javafx.application.Platform.exit();
        }
    }
}


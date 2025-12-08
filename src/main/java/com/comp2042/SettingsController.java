package com.comp2042;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.Parent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Settings scene.
 * 
 * <p>Manages all game settings including:
 * <ul>
 *   <li>Ghost Piece - Toggle to show/hide the ghost piece preview</li>
 *   <li>Hard Drop - Toggle to enable/disable hard drop functionality</li>
 *   <li>Difficulty - Select between EASY, MEDIUM, and HARD difficulty levels</li>
 * </ul>
 * </p>
 * 
 * <p>All settings are automatically saved using Java Preferences API and persist
 * between application sessions. Changes apply immediately during gameplay.</p>
 * 
 * @author Tetris Game Team
 * @version 1.0
 */
public class SettingsController implements Initializable {
    
    @FXML
    private Button backButton;
    
    @FXML
    private ToggleButton ghostToggle;
    
    @FXML
    private ToggleButton hardDropToggle;
    
    private ToggleGroup difficultyGroup;
    
    @FXML
    private RadioButton easyRadio;
    
    @FXML
    private RadioButton mediumRadio;
    
    @FXML
    private RadioButton hardRadio;
    
    @FXML
    private VBox difficultySection;
    
    // Removed primaryStage field - we get Stage from scene.getWindow()
    private SettingsManager settingsManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("SettingsController.initialize() called!");
        try {
            System.out.println("Getting SettingsManager instance...");
            try {
                settingsManager = SettingsManager.getInstance();
                System.out.println("SettingsManager obtained: " + settingsManager);
            } catch (Exception e) {
                System.err.println("WARNING: Error getting SettingsManager: " + e.getMessage());
                e.printStackTrace();
                // Continue - settings can work without SettingsManager
            }
            
            // Initialize ghost piece toggle with saved setting (only if settingsManager is available)
            if (settingsManager != null) {
                if (ghostToggle != null) {
                    boolean ghostEnabled = settingsManager.isGhostPieceEnabled();
                    ghostToggle.setSelected(ghostEnabled);
                    ghostToggle.setText(ghostEnabled ? "ON" : "OFF");
                }
                
                // Initialize hard drop toggle with saved setting
                if (hardDropToggle != null) {
                    boolean hardDropEnabled = settingsManager.isHardDropEnabled();
                    hardDropToggle.setSelected(hardDropEnabled);
                    hardDropToggle.setText(hardDropEnabled ? "ON" : "OFF");
                }
                
                // Create ToggleGroup for difficulty radio buttons
                difficultyGroup = new ToggleGroup();
                
                // Initialize difficulty radio buttons
                SettingsManager.Difficulty currentDifficulty = settingsManager.getDifficulty();
                if (easyRadio != null) {
                    easyRadio.setToggleGroup(difficultyGroup);
                    easyRadio.setSelected(currentDifficulty == SettingsManager.Difficulty.EASY);
                }
                if (mediumRadio != null) {
                    mediumRadio.setToggleGroup(difficultyGroup);
                    mediumRadio.setSelected(currentDifficulty == SettingsManager.Difficulty.MEDIUM);
                }
                if (hardRadio != null) {
                    hardRadio.setToggleGroup(difficultyGroup);
                    hardRadio.setSelected(currentDifficulty == SettingsManager.Difficulty.HARD);
                }
                
                // Update difficulty section styling based on selection
                updateDifficultySectionStyle();
                
            } else {
                // If settingsManager is null, initialize with defaults
                System.err.println("WARNING: SettingsManager is null, using default values");
                if (ghostToggle != null) {
                    ghostToggle.setSelected(true);
                    ghostToggle.setText("ON");
                }
                if (hardDropToggle != null) {
                    hardDropToggle.setSelected(true);
                    hardDropToggle.setText("ON");
                }
                difficultyGroup = new ToggleGroup();
                if (mediumRadio != null) {
                    mediumRadio.setToggleGroup(difficultyGroup);
                    mediumRadio.setSelected(true);
                }
            }
        
            // Ghost piece toggle listener - saves immediately (only if settingsManager available)
            if (settingsManager != null && ghostToggle != null) {
                ghostToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        settingsManager.setGhostPieceEnabled(newValue);
                        ghostToggle.setText(newValue ? "ON" : "OFF");
                    }
                });
            }
            
            // Hard drop toggle listener - saves immediately (only if settingsManager available)
            if (settingsManager != null && hardDropToggle != null) {
                hardDropToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        settingsManager.setHardDropEnabled(newValue);
                        hardDropToggle.setText(newValue ? "ON" : "OFF");
                    }
                });
            }
            
            // Difficulty radio button listeners - save immediately when changed (only if settingsManager available)
            if (settingsManager != null && difficultyGroup != null) {
                difficultyGroup.selectedToggleProperty().addListener(new ChangeListener<javafx.scene.control.Toggle>() {
                    @Override
                    public void changed(ObservableValue<? extends javafx.scene.control.Toggle> observable, 
                                       javafx.scene.control.Toggle oldValue, javafx.scene.control.Toggle newValue) {
                        // Determine which difficulty was selected
                        SettingsManager.Difficulty selectedDifficulty = SettingsManager.Difficulty.MEDIUM;  // Default
                        if (easyRadio != null && easyRadio.isSelected()) {
                            selectedDifficulty = SettingsManager.Difficulty.EASY;
                        } else if (mediumRadio != null && mediumRadio.isSelected()) {
                            selectedDifficulty = SettingsManager.Difficulty.MEDIUM;
                        } else if (hardRadio != null && hardRadio.isSelected()) {
                            selectedDifficulty = SettingsManager.Difficulty.HARD;
                        }
                        
                        // Save difficulty immediately
                        settingsManager.setDifficulty(selectedDifficulty);
                        
                        // Update styling
                        updateDifficultySectionStyle();
                        
                        // Immediately update game speed if game is running
                        GuiController.updateDifficultySpeedIfActive();
                    }
                });
            }
            
            // Ensure all controls are enabled and clickable
            if (ghostToggle != null) {
                ghostToggle.setDisable(false);
                ghostToggle.setMouseTransparent(false);
            }
            if (hardDropToggle != null) {
                hardDropToggle.setDisable(false);
                hardDropToggle.setMouseTransparent(false);
            }
            if (easyRadio != null) {
                easyRadio.setDisable(false);
                easyRadio.setMouseTransparent(false);
            }
            if (mediumRadio != null) {
                mediumRadio.setDisable(false);
                mediumRadio.setMouseTransparent(false);
            }
            if (hardRadio != null) {
                hardRadio.setDisable(false);
                hardRadio.setMouseTransparent(false);
            }
            if (backButton != null) {
                backButton.setDisable(false);
                backButton.setMouseTransparent(false);
                backButton.setVisible(true);
                backButton.setManaged(true);
                // Ensure the button has the event handler
                if (backButton.getOnAction() == null) {
                    backButton.setOnAction(this::onBackClick);
                }
            }
            
            System.out.println("SettingsController.initialize() completed successfully!");
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("CRITICAL ERROR in SettingsController.initialize():");
            System.err.println("========================================");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");
            // Don't rethrow - let the scene load even if initialization partially fails
        }
    }
    
    /**
     * Updates the difficulty section styling based on current selection.
     * No border styling - removed to prevent rectangular box appearance.
     */
    private void updateDifficultySectionStyle() {
        if (difficultySection == null) return;
        
        // Remove any border styling - keep it clean without rectangular boxes
        difficultySection.setStyle("-fx-border-color: transparent; -fx-border-width: 0px; -fx-padding: 8px;");
    }
    
    /**
     * Sets the primary stage for scene switching.
     */
    // Removed setPrimaryStage - we now get Stage from scene.getWindow()
    
    /**
     * Handles the Back button click - returns to main menu.
     * Settings are already saved automatically when changed.
     */
    @FXML
    private void onBackClick(ActionEvent event) {
        // IMMEDIATELY prevent minimization before any operations
        Stage primaryStage = StageManager.getPrimaryStage();
        if (primaryStage != null) {
            if (primaryStage.isIconified()) {
                primaryStage.setIconified(false);
            }
            primaryStage.toFront();
        }
        
        // Use preloaded main menu root for instant switching (root swap, not scene replacement)
        javafx.application.Platform.runLater(() -> {
            Parent mainMenuRoot = SceneManager.getPreloadedRoot("mainMenu");
            if (mainMenuRoot == null) {
                System.err.println("ERROR: Main menu root not preloaded!");
                return;
            }
            
            // Swap root node in SINGLE scene - this prevents macOS window recreation
            StageManager.switchRoot(mainMenuRoot, "Tetris - Main Menu");
            
            // Update resume button visibility immediately and again after root swap completes
            MainMenuController mainMenuController = SceneManager.getMainMenuController();
            if (mainMenuController != null) {
                // Update immediately (if we're on JavaFX thread)
                if (javafx.application.Platform.isFxApplicationThread()) {
                    mainMenuController.updateResumeButtonVisibility();
                }
                // Also update after root swap completes (nested Platform.runLater)
                javafx.application.Platform.runLater(() -> {
                    javafx.application.Platform.runLater(() -> {
                        mainMenuController.updateResumeButtonVisibility();
                    });
                });
            } else {
                System.err.println("ERROR: MainMenuController is null when returning from settings!");
            }
        });
    }
}


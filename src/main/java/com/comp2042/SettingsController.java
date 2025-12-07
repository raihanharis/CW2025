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

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Settings scene.
 * Handles volume control, music toggle, and SFX toggle.
 */
public class SettingsController implements Initializable {
    
    @FXML
    private Slider volumeSlider;
    
    @FXML
    private Label volumeLabel;
    
    @FXML
    private ToggleButton musicToggle;
    
    @FXML
    private ToggleButton sfxToggle;
    
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
    
    private Stage primaryStage;
    private AudioManager audioManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("SettingsController.initialize() called!");
        try {
            System.out.println("Getting AudioManager instance...");
            audioManager = AudioManager.getInstance();
            System.out.println("AudioManager obtained: " + audioManager);
            
            // Initialize volume slider with current volume
            if (volumeSlider != null) {
                double currentVolume = audioManager.getMasterVolume() * 100.0;  // Convert to 0-100
                volumeSlider.setValue(currentVolume);
            }
            
            if (volumeLabel != null) {
                volumeLabel.setText(String.format("%.0f%%", audioManager.getMasterVolume() * 100.0));
            }
            
            // Initialize toggles with current state
            if (musicToggle != null) {
                boolean musicEnabled = audioManager.isMusicEnabled();
                musicToggle.setSelected(musicEnabled);
                musicToggle.setText(musicEnabled ? "ON" : "OFF");
            }
            
            if (sfxToggle != null) {
                boolean sfxEnabled = audioManager.isSfxEnabled();
                sfxToggle.setSelected(sfxEnabled);
                sfxToggle.setText(sfxEnabled ? "ON" : "OFF");
            }
            
            // Initialize ghost piece toggle with saved setting
            if (ghostToggle != null) {
                boolean ghostEnabled = audioManager.isGhostPieceEnabled();
                ghostToggle.setSelected(ghostEnabled);
                ghostToggle.setText(ghostEnabled ? "ON" : "OFF");
            }
            
            // Initialize hard drop toggle with saved setting
            if (hardDropToggle != null) {
                boolean hardDropEnabled = audioManager.isHardDropEnabled();
                hardDropToggle.setSelected(hardDropEnabled);
                hardDropToggle.setText(hardDropEnabled ? "ON" : "OFF");
            }
            
            // Create ToggleGroup for difficulty radio buttons
            difficultyGroup = new ToggleGroup();
            
            // Initialize difficulty radio buttons
            AudioManager.Difficulty currentDifficulty = audioManager.getDifficulty();
            if (easyRadio != null) {
                easyRadio.setToggleGroup(difficultyGroup);
                easyRadio.setSelected(currentDifficulty == AudioManager.Difficulty.EASY);
            }
            if (mediumRadio != null) {
                mediumRadio.setToggleGroup(difficultyGroup);
                mediumRadio.setSelected(currentDifficulty == AudioManager.Difficulty.MEDIUM);
            }
            if (hardRadio != null) {
                hardRadio.setToggleGroup(difficultyGroup);
                hardRadio.setSelected(currentDifficulty == AudioManager.Difficulty.HARD);
            }
            
            // Update difficulty section styling based on selection
            updateDifficultySectionStyle();
        
            // Volume slider listener - saves immediately
            if (volumeSlider != null && volumeLabel != null) {
                volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        double volumePercent = newValue.doubleValue();
                        double volume = volumePercent / 100.0;  // Convert to 0.0-1.0
                        // Update volume immediately and save
                        audioManager.setMasterVolume(volume);
                        // Update label
                        volumeLabel.setText(String.format("%.0f%%", volumePercent));
                    }
                });
            }
            
            // Music toggle listener - saves immediately
            if (musicToggle != null) {
                musicToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        audioManager.setMusicEnabled(newValue);
                        musicToggle.setText(newValue ? "ON" : "OFF");
                    }
                });
            }
            
            // SFX toggle listener - saves immediately
            if (sfxToggle != null) {
                sfxToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        audioManager.setSfxEnabled(newValue);
                        sfxToggle.setText(newValue ? "ON" : "OFF");
                    }
                });
            }
            
            // Ghost piece toggle listener - saves immediately
            if (ghostToggle != null) {
                ghostToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        audioManager.setGhostPieceEnabled(newValue);
                        ghostToggle.setText(newValue ? "ON" : "OFF");
                    }
                });
            }
            
            // Hard drop toggle listener - saves immediately
            if (hardDropToggle != null) {
                hardDropToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        audioManager.setHardDropEnabled(newValue);
                        hardDropToggle.setText(newValue ? "ON" : "OFF");
                    }
                });
            }
            
            // Difficulty radio button listeners - save immediately when changed
            if (difficultyGroup != null) {
                difficultyGroup.selectedToggleProperty().addListener(new ChangeListener<javafx.scene.control.Toggle>() {
                    @Override
                    public void changed(ObservableValue<? extends javafx.scene.control.Toggle> observable, 
                                       javafx.scene.control.Toggle oldValue, javafx.scene.control.Toggle newValue) {
                        // Determine which difficulty was selected
                        AudioManager.Difficulty selectedDifficulty = AudioManager.Difficulty.MEDIUM;  // Default
                        if (easyRadio != null && easyRadio.isSelected()) {
                            selectedDifficulty = AudioManager.Difficulty.EASY;
                        } else if (mediumRadio != null && mediumRadio.isSelected()) {
                            selectedDifficulty = AudioManager.Difficulty.MEDIUM;
                        } else if (hardRadio != null && hardRadio.isSelected()) {
                            selectedDifficulty = AudioManager.Difficulty.HARD;
                        }
                        
                        // Save difficulty immediately
                        audioManager.setDifficulty(selectedDifficulty);
                        
                        // Update styling
                        updateDifficultySectionStyle();
                    }
                });
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
     * Adds red border for HARD mode.
     */
    private void updateDifficultySectionStyle() {
        if (difficultySection == null) return;
        
        boolean isHardSelected = hardRadio != null && hardRadio.isSelected();
        
        if (isHardSelected) {
            difficultySection.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2px; -fx-border-radius: 6px; -fx-padding: 8px;");
        } else {
            difficultySection.setStyle("-fx-border-color: transparent; -fx-border-width: 0px; -fx-padding: 0px;");
        }
    }
    
    /**
     * Sets the primary stage for scene switching.
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    /**
     * Handles the Back button click - returns to main menu.
     * Settings are already saved automatically when changed.
     */
    @FXML
    private void onBackClick(ActionEvent event) {
        // Navigate back to main menu
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/mainMenu.fxml")
            );
            
            javafx.scene.Parent mainMenuRoot = loader.load();
            
            // Get the MainMenuController and set the primary stage
            MainMenuController mainMenuController = loader.getController();
            mainMenuController.setPrimaryStage(primaryStage);
            
            // Create and set the main menu scene
            javafx.scene.Scene mainMenuScene = new javafx.scene.Scene(mainMenuRoot, 900, 700);
            mainMenuScene.setFill(javafx.scene.paint.Color.web("#000000"));
            
            if (primaryStage != null) {
                // Set fullscreen BEFORE scene change to prevent exit
                primaryStage.setFullScreen(true);
                primaryStage.setFullScreenExitHint("");
                primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
                
                primaryStage.setScene(mainMenuScene);
                primaryStage.setTitle("Tetris - Main Menu");
                
                // Force fullscreen immediately after scene change
                javafx.application.Platform.runLater(() -> {
                    primaryStage.setFullScreen(true);
                    primaryStage.setFullScreenExitHint("");
                    primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
                });
                
                mainMenuRoot.requestFocus();
            }
            
        } catch (Exception e) {
            System.err.println("Error loading main menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


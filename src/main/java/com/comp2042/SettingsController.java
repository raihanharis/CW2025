package com.comp2042;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
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
    
    private Stage primaryStage;
    private AudioManager audioManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        audioManager = AudioManager.getInstance();
        
        // Initialize volume slider with current volume
        double currentVolume = audioManager.getMasterVolume() * 100.0;  // Convert to 0-100
        volumeSlider.setValue(currentVolume);
        volumeLabel.setText(String.format("%.0f%%", currentVolume));
        
        // Initialize toggles with current state
        musicToggle.setSelected(audioManager.isMusicEnabled());
        musicToggle.setText(audioManager.isMusicEnabled() ? "ON" : "OFF");
        
        sfxToggle.setSelected(audioManager.isSfxEnabled());
        sfxToggle.setText(audioManager.isSfxEnabled() ? "ON" : "OFF");
        
        // Initialize ghost piece toggle with saved setting (using ToggleButton like Music/SFX)
        boolean ghostEnabled = audioManager.isGhostPieceEnabled();
        ghostToggle.setSelected(ghostEnabled);
        ghostToggle.setText(ghostEnabled ? "ON" : "OFF");
        
        // Volume slider listener - updates volume instantly
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double volumePercent = newValue.doubleValue();
                double volume = volumePercent / 100.0;  // Convert to 0.0-1.0
                
                // Update volume immediately
                audioManager.setMasterVolume(volume);
                
                // Update label
                volumeLabel.setText(String.format("%.0f%%", volumePercent));
            }
        });
        
        // Music toggle listener
        musicToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                audioManager.setMusicEnabled(newValue);
                musicToggle.setText(newValue ? "ON" : "OFF");
            }
        });
        
        // SFX toggle listener
        sfxToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                audioManager.setSfxEnabled(newValue);
                sfxToggle.setText(newValue ? "ON" : "OFF");
            }
        });
        
        // Ghost piece toggle listener (same pattern as Music/SFX)
        ghostToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                audioManager.setGhostPieceEnabled(newValue);
                ghostToggle.setText(newValue ? "ON" : "OFF");
            }
        });
    }
    
    /**
     * Sets the primary stage for scene switching.
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    /**
     * Handles the Back button click - returns to main menu.
     */
    @FXML
    private void onBackClick(ActionEvent event) {
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
                primaryStage.setScene(mainMenuScene);
                primaryStage.setTitle("Tetris - Main Menu");
                mainMenuRoot.requestFocus();
            }
            
        } catch (Exception e) {
            System.err.println("Error loading main menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


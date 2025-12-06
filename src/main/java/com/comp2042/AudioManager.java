package com.comp2042;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.util.prefs.Preferences;

/**
 * Manages all audio in the game: background music and sound effects.
 * Handles volume control, music toggle, and SFX toggle with persistence.
 */
public class AudioManager {
    
    private static AudioManager instance;
    
    private MediaPlayer backgroundMusic;
    private double masterVolume = 1.0;  // 0.0 to 1.0
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;
    private boolean ghostPieceEnabled = true;  // Default ON
    
    private Preferences prefs;
    
    private AudioManager() {
        prefs = Preferences.userNodeForPackage(AudioManager.class);
        loadSettings();
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    /**
     * Loads settings from preferences.
     */
    private void loadSettings() {
        masterVolume = prefs.getDouble("masterVolume", 1.0);
        musicEnabled = prefs.getBoolean("musicEnabled", true);
        sfxEnabled = prefs.getBoolean("sfxEnabled", true);
        ghostPieceEnabled = prefs.getBoolean("ghostPieceEnabled", true);  // Default ON
    }
    
    /**
     * Saves settings to preferences.
     */
    private void saveSettings() {
        prefs.putDouble("masterVolume", masterVolume);
        prefs.putBoolean("musicEnabled", musicEnabled);
        prefs.putBoolean("sfxEnabled", sfxEnabled);
        prefs.putBoolean("ghostPieceEnabled", ghostPieceEnabled);
    }
    
    /**
     * Sets the master volume (0.0 to 1.0).
     * Updates background music volume immediately.
     */
    public void setMasterVolume(double volume) {
        if (volume < 0.0) volume = 0.0;
        if (volume > 1.0) volume = 1.0;
        
        this.masterVolume = volume;
        
        // Update background music volume immediately
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(volume);
        }
        
        saveSettings();
    }
    
    /**
     * Gets the master volume (0.0 to 1.0).
     */
    public double getMasterVolume() {
        return masterVolume;
    }
    
    /**
     * Sets whether music is enabled.
     * If disabled, pauses music immediately.
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        
        if (backgroundMusic != null) {
            if (enabled) {
                backgroundMusic.play();
            } else {
                backgroundMusic.pause();
            }
        }
        
        saveSettings();
    }
    
    /**
     * Gets whether music is enabled.
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    /**
     * Sets whether SFX are enabled.
     */
    public void setSfxEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
        saveSettings();
    }
    
    /**
     * Gets whether SFX are enabled.
     */
    public boolean isSfxEnabled() {
        return sfxEnabled;
    }
    
    /**
     * Sets whether the ghost piece is enabled.
     */
    public void setGhostPieceEnabled(boolean enabled) {
        this.ghostPieceEnabled = enabled;
        saveSettings();
    }
    
    /**
     * Gets whether the ghost piece is enabled.
     */
    public boolean isGhostPieceEnabled() {
        return ghostPieceEnabled;
    }
    
    /**
     * Starts background music (if music is enabled).
     * Music will loop continuously.
     */
    public void startBackgroundMusic(String musicPath) {
        try {
            if (backgroundMusic != null) {
                backgroundMusic.stop();
            }
            
            Media media = new Media(getClass().getResource(musicPath).toExternalForm());
            backgroundMusic = new MediaPlayer(media);
            backgroundMusic.setVolume(masterVolume);
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);  // Loop forever
            
            if (musicEnabled) {
                backgroundMusic.play();
            }
        } catch (Exception e) {
            System.err.println("Error loading background music: " + e.getMessage());
            // Continue without music if file not found
        }
    }
    
    /**
     * Stops background music.
     */
    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }
    
    /**
     * Plays a sound effect (if SFX are enabled).
     */
    public void playSoundEffect(String soundPath) {
        if (!sfxEnabled) {
            return;  // SFX disabled
        }
        
        try {
            Media media = new Media(getClass().getResource(soundPath).toExternalForm());
            MediaPlayer soundPlayer = new MediaPlayer(media);
            soundPlayer.setVolume(masterVolume);
            soundPlayer.play();
        } catch (Exception e) {
            // Silently fail if sound file not found
            // System.err.println("Error playing sound effect: " + e.getMessage());
        }
    }
}


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
    private boolean hardDropEnabled = true;  // Default ON
    
    // Difficulty levels
    public enum Difficulty {
        EASY(700),    // 700ms per step
        MEDIUM(400),  // 400ms per step (default)
        HARD(200);    // 200ms per step
        
        private final int dropSpeedMs;
        
        Difficulty(int dropSpeedMs) {
            this.dropSpeedMs = dropSpeedMs;
        }
        
        public int getDropSpeedMs() {
            return dropSpeedMs;
        }
    }
    
    private Difficulty difficulty = Difficulty.MEDIUM;  // Default MEDIUM
    
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
        hardDropEnabled = prefs.getBoolean("hardDropEnabled", true);  // Default ON
        
        // Load difficulty (default MEDIUM)
        String difficultyStr = prefs.get("difficulty", "MEDIUM");
        try {
            difficulty = Difficulty.valueOf(difficultyStr);
        } catch (IllegalArgumentException e) {
            difficulty = Difficulty.MEDIUM;  // Fallback to MEDIUM
        }
    }
    
    /**
     * Saves settings to preferences.
     */
    private void saveSettingsInternal() {
        prefs.putDouble("masterVolume", masterVolume);
        prefs.putBoolean("musicEnabled", musicEnabled);
        prefs.putBoolean("sfxEnabled", sfxEnabled);
        prefs.putBoolean("ghostPieceEnabled", ghostPieceEnabled);
        prefs.putBoolean("hardDropEnabled", hardDropEnabled);
        prefs.put("difficulty", difficulty.name());
    }
    
    /**
     * Public method to save settings (called from SettingsController).
     */
    public void saveSettings() {
        saveSettingsInternal();
    }
    
    /**
     * Sets the master volume (0.0 to 1.0).
     * Updates background music volume immediately.
     * @param save if true, saves to preferences; if false, only updates in-memory value
     */
    public void setMasterVolume(double volume, boolean save) {
        if (volume < 0.0) volume = 0.0;
        if (volume > 1.0) volume = 1.0;
        
        this.masterVolume = volume;
        
        // Update background music volume immediately
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(volume);
        }
        
        if (save) {
            saveSettingsInternal();
        }
    }
    
    /**
     * Sets the master volume (0.0 to 1.0) and saves immediately.
     * Updates background music volume immediately.
     */
    public void setMasterVolume(double volume) {
        setMasterVolume(volume, true);
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
     * @param save if true, saves to preferences; if false, only updates in-memory value
     */
    public void setMusicEnabled(boolean enabled, boolean save) {
        this.musicEnabled = enabled;
        
        if (backgroundMusic != null) {
            if (enabled) {
                backgroundMusic.play();
            } else {
                backgroundMusic.pause();
            }
        }
        
        if (save) {
            saveSettingsInternal();
        }
    }
    
    /**
     * Sets whether music is enabled and saves immediately.
     * If disabled, pauses music immediately.
     */
    public void setMusicEnabled(boolean enabled) {
        setMusicEnabled(enabled, true);
    }
    
    /**
     * Gets whether music is enabled.
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    /**
     * Sets whether SFX are enabled.
     * @param save if true, saves to preferences; if false, only updates in-memory value
     */
    public void setSfxEnabled(boolean enabled, boolean save) {
        this.sfxEnabled = enabled;
        if (save) {
            saveSettingsInternal();
        }
    }
    
    /**
     * Sets whether SFX are enabled and saves immediately.
     */
    public void setSfxEnabled(boolean enabled) {
        setSfxEnabled(enabled, true);
    }
    
    /**
     * Gets whether SFX are enabled.
     */
    public boolean isSfxEnabled() {
        return sfxEnabled;
    }
    
    /**
     * Sets whether the ghost piece is enabled.
     * @param save if true, saves to preferences; if false, only updates in-memory value
     */
    public void setGhostPieceEnabled(boolean enabled, boolean save) {
        this.ghostPieceEnabled = enabled;
        if (save) {
            saveSettingsInternal();
        }
    }
    
    /**
     * Sets whether the ghost piece is enabled and saves immediately.
     */
    public void setGhostPieceEnabled(boolean enabled) {
        setGhostPieceEnabled(enabled, true);
    }
    
    /**
     * Gets whether the ghost piece is enabled.
     */
    public boolean isGhostPieceEnabled() {
        return ghostPieceEnabled;
    }
    
    /**
     * Sets whether hard drop is enabled.
     * @param save if true, saves to preferences; if false, only updates in-memory value
     */
    public void setHardDropEnabled(boolean enabled, boolean save) {
        this.hardDropEnabled = enabled;
        if (save) {
            saveSettingsInternal();
        }
    }
    
    /**
     * Sets whether hard drop is enabled and saves immediately.
     */
    public void setHardDropEnabled(boolean enabled) {
        setHardDropEnabled(enabled, true);
    }
    
    /**
     * Gets whether hard drop is enabled.
     */
    public boolean isHardDropEnabled() {
        return hardDropEnabled;
    }
    
    /**
     * Sets the difficulty level.
     * @param save if true, saves to preferences; if false, only updates in-memory value
     */
    public void setDifficulty(Difficulty newDifficulty, boolean save) {
        if (newDifficulty == null) {
            newDifficulty = Difficulty.MEDIUM;
        }
        this.difficulty = newDifficulty;
        if (save) {
            saveSettingsInternal();
        }
    }
    
    /**
     * Sets the difficulty level and saves immediately.
     */
    public void setDifficulty(Difficulty newDifficulty) {
        setDifficulty(newDifficulty, true);
    }
    
    /**
     * Gets the current difficulty level.
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    /**
     * Gets the drop speed in milliseconds for the current difficulty.
     */
    public int getDropSpeedMs() {
        return difficulty.getDropSpeedMs();
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


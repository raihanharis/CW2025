package com.comp2042;

import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.prefs.Preferences;

/**
 * Manages all audio in the game: sound effects.
 * Handles volume control and SFX toggle with persistence.
 * All audio operations are thread-safe and UI-safe - failures won't break the game.
 */
public class AudioManager {
    
    private static AudioManager instance;
    
    private double masterVolume = 1.0;  // 0.0 to 1.0
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
    private Skin selectedSkin = Skin.CLASSIC;  // Default CLASSIC skin
    
    private Preferences prefs;
    
    private AudioManager() {
        try {
            prefs = Preferences.userNodeForPackage(AudioManager.class);
            loadSettings();
        } catch (Exception e) {
            System.err.println("WARNING: Error initializing AudioManager: " + e.getMessage());
            // Continue with defaults
        }
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            synchronized (AudioManager.class) {
                if (instance == null) {
                    instance = new AudioManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Loads settings from preferences.
     */
    private void loadSettings() {
        try {
            masterVolume = prefs.getDouble("masterVolume", 1.0);
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
            
            // Load skin (default CLASSIC)
            String skinStr = prefs.get("skin", "CLASSIC");
            try {
                selectedSkin = Skin.valueOf(skinStr);
            } catch (IllegalArgumentException e) {
                selectedSkin = Skin.CLASSIC;  // Fallback to CLASSIC
            }
        } catch (Exception e) {
            System.err.println("WARNING: Error loading audio settings: " + e.getMessage());
            // Use defaults
        }
    }
    
    /**
     * Saves settings to preferences.
     */
    private void saveSettingsInternal() {
        try {
            prefs.putDouble("masterVolume", masterVolume);
            prefs.putBoolean("sfxEnabled", sfxEnabled);
            prefs.putBoolean("ghostPieceEnabled", ghostPieceEnabled);
            prefs.putBoolean("hardDropEnabled", hardDropEnabled);
            prefs.put("difficulty", difficulty.name());
            prefs.put("skin", selectedSkin.name());
        } catch (Exception e) {
            System.err.println("WARNING: Error saving audio settings: " + e.getMessage());
        }
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
        try {
            if (volume < 0.0) volume = 0.0;
            if (volume > 1.0) volume = 1.0;
            
            this.masterVolume = volume;
            
            if (save) {
                saveSettingsInternal();
            }
        } catch (Exception e) {
            System.err.println("WARNING: Error setting master volume: " + e.getMessage());
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
     * Sets whether SFX are enabled.
     * @param save if true, saves to preferences; if false, only updates in-memory value
     */
    public void setSfxEnabled(boolean enabled, boolean save) {
        try {
            this.sfxEnabled = enabled;
            if (save) {
                saveSettingsInternal();
            }
        } catch (Exception e) {
            System.err.println("WARNING: Error setting SFX enabled: " + e.getMessage());
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
        try {
            this.ghostPieceEnabled = enabled;
            if (save) {
                saveSettingsInternal();
            }
        } catch (Exception e) {
            System.err.println("WARNING: Error setting ghost piece enabled: " + e.getMessage());
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
        try {
            this.hardDropEnabled = enabled;
            if (save) {
                saveSettingsInternal();
            }
        } catch (Exception e) {
            System.err.println("WARNING: Error setting hard drop enabled: " + e.getMessage());
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
        try {
            if (newDifficulty == null) {
                newDifficulty = Difficulty.MEDIUM;
            }
            this.difficulty = newDifficulty;
            if (save) {
                saveSettingsInternal();
            }
        } catch (Exception e) {
            System.err.println("WARNING: Error setting difficulty: " + e.getMessage());
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
     * Sets the selected skin.
     * @param newSkin The skin to use
     * @param save if true, saves to preferences; if false, only updates in-memory value
     */
    public void setSkin(Skin newSkin, boolean save) {
        try {
            if (newSkin == null) {
                newSkin = Skin.CLASSIC;
            }
            this.selectedSkin = newSkin;
            if (save) {
                saveSettingsInternal();
            }
        } catch (Exception e) {
            System.err.println("WARNING: Error setting skin: " + e.getMessage());
        }
    }
    
    /**
     * Sets the selected skin and saves immediately.
     */
    public void setSkin(Skin newSkin) {
        setSkin(newSkin, true);
    }
    
    /**
     * Gets the currently selected skin.
     */
    public Skin getSkin() {
        return selectedSkin;
    }
    
    /**
     * REMOVED: Music functionality has been removed.
     */
    @Deprecated
    public void startBackgroundMusic(String musicPath) {
        // Music functionality removed - do nothing
    }
    
    /**
     * REMOVED: Music functionality has been removed.
     */
    @Deprecated
    public void stopBackgroundMusic() {
        // Music functionality removed - do nothing
    }
    
    /**
     * REMOVED: Music functionality has been removed.
     */
    @Deprecated
    public void stopMusic() {
        // Music functionality removed - do nothing
    }
    
    /**
     * Plays a sound effect (if SFX are enabled).
     * This method is safe, non-blocking, and never throws exceptions.
     * If the file is missing, it silently fails without freezing the UI.
     */
    public void playSoundEffect(String soundPath) {
        // Check if SFX are enabled
        if (!sfxEnabled) {
            return;  // SFX disabled
        }
        
        // Play sound effect asynchronously to avoid blocking UI
        javafx.application.Platform.runLater(() -> {
            try {
                // Check for null resource before creating Media object
                java.net.URL soundUrl = null;
                try {
                    soundUrl = getClass().getResource(soundPath);
                } catch (Exception e) {
                    // Silently fail - sound file not found
                    return;
                }
                
                if (soundUrl == null) {
                    // Silently fail - sound file not found
                    return;
                }
                
                // Convert URL to string safely
                String mediaUrl;
                try {
                    mediaUrl = soundUrl.toExternalForm();
                } catch (Exception e) {
                    // Silently fail - URL conversion error
                    return;
                }
                
                // Create Media object with error handling
                javafx.scene.media.Media media;
                try {
                    media = new javafx.scene.media.Media(mediaUrl);
                } catch (Exception e) {
                    // Silently fail - invalid media format
                    return;
                }
                
                // Set up error handler
                media.setOnError(() -> {
                    // Silently fail on media error
                });
                
                // Create MediaPlayer with error handling
                javafx.scene.media.MediaPlayer soundPlayer;
                try {
                    soundPlayer = new javafx.scene.media.MediaPlayer(media);
                } catch (Exception e) {
                    // Silently fail - MediaPlayer creation error
                    return;
                }
                
                // Configure and play
                try {
                    soundPlayer.setVolume(masterVolume);
                    soundPlayer.setOnError(() -> {
                        // Silently fail on playback error
                    });
                    soundPlayer.play();
                } catch (Exception e) {
                    // Silently fail - playback error
                }
                
            } catch (Exception e) {
                // Silently fail if sound file not found or any other error
                // Don't print errors for sound effects - they're not critical
            }
        });
    }
}

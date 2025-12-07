package com.comp2042;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.util.prefs.Preferences;

/**
 * Manages all audio in the game: background music and sound effects.
 * Handles volume control, music toggle, and SFX toggle with persistence.
 * All audio operations are thread-safe and UI-safe - failures won't break the game.
 */
public class AudioManager {
    
    private static AudioManager instance;
    
    private MediaPlayer backgroundMusic;
    private Media backgroundMedia;
    private double masterVolume = 1.0;  // 0.0 to 1.0
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;
    private boolean ghostPieceEnabled = true;  // Default ON
    private boolean hardDropEnabled = true;  // Default ON
    private boolean isMusicLoading = false;  // Prevent multiple simultaneous loads
    
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
            prefs.putBoolean("musicEnabled", musicEnabled);
            prefs.putBoolean("sfxEnabled", sfxEnabled);
            prefs.putBoolean("ghostPieceEnabled", ghostPieceEnabled);
            prefs.putBoolean("hardDropEnabled", hardDropEnabled);
            prefs.put("difficulty", difficulty.name());
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
            
            final double finalVolume = volume;  // Make final for lambda
            this.masterVolume = finalVolume;
            
            // Update background music volume immediately (thread-safe)
            if (backgroundMusic != null) {
                if (javafx.application.Platform.isFxApplicationThread()) {
                    backgroundMusic.setVolume(finalVolume);
                } else {
                    javafx.application.Platform.runLater(() -> {
                        if (backgroundMusic != null) {
                            backgroundMusic.setVolume(finalVolume);
                        }
                    });
                }
            }
            
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
     * Sets whether music is enabled.
     * If disabled, pauses music immediately.
     * @param save if true, saves to preferences; if false, only updates in-memory value
     */
    public void setMusicEnabled(boolean enabled, boolean save) {
        try {
            this.musicEnabled = enabled;
            
            // Update background music playback (thread-safe)
            if (backgroundMusic != null) {
                if (javafx.application.Platform.isFxApplicationThread()) {
                    if (enabled) {
                        backgroundMusic.play();
                    } else {
                        backgroundMusic.pause();
                    }
                } else {
                    javafx.application.Platform.runLater(() -> {
                        if (backgroundMusic != null) {
                            if (enabled) {
                                backgroundMusic.play();
                            } else {
                                backgroundMusic.pause();
                            }
                        }
                    });
                }
            }
            
            if (save) {
                saveSettingsInternal();
            }
        } catch (Exception e) {
            System.err.println("WARNING: Error setting music enabled: " + e.getMessage());
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
     * Starts background music (if music is enabled).
     * Music will loop continuously.
     * This method is safe, non-blocking, and will only create one MediaPlayer instance.
     * If called multiple times, it reuses the existing MediaPlayer.
     */
    public void startBackgroundMusic(String musicPath) {
        // If already loading or loaded, return immediately
        if (isMusicLoading || backgroundMusic != null) {
            // If already loaded, just play if enabled
            if (backgroundMusic != null && musicEnabled) {
                try {
                    if (javafx.application.Platform.isFxApplicationThread()) {
                        backgroundMusic.play();
                    } else {
                        javafx.application.Platform.runLater(() -> {
                            if (backgroundMusic != null && musicEnabled) {
                                backgroundMusic.play();
                            }
                        });
                    }
                } catch (Exception e) {
                    System.err.println("WARNING: Error playing existing background music: " + e.getMessage());
                }
            }
            return;
        }
        
        // Mark as loading
        isMusicLoading = true;
        
        // Load music asynchronously on JavaFX thread to avoid blocking UI
        javafx.application.Platform.runLater(() -> {
            try {
                // Check for null resource before creating Media object
                java.net.URL musicUrl = null;
                try {
                    musicUrl = getClass().getResource(musicPath);
                } catch (Exception e) {
                    System.err.println("WARNING: Error getting music resource: " + e.getMessage());
                }
                
                if (musicUrl == null) {
                    System.out.println("WARNING: Background music file not found at: " + musicPath);
                    System.out.println("WARNING: Game will continue without background music.");
                    isMusicLoading = false;
                    return;  // Continue without music - don't break the game
                }
                
                // Convert URL to string safely
                String mediaUrl;
                try {
                    mediaUrl = musicUrl.toExternalForm();
                } catch (Exception e) {
                    System.err.println("WARNING: Error converting music URL to string: " + e.getMessage());
                    isMusicLoading = false;
                    return;
                }
                
                // Create Media object with error handling
                Media media;
                try {
                    media = new Media(mediaUrl);
                    backgroundMedia = media;
                } catch (Exception e) {
                    System.err.println("WARNING: Could not create Media object: " + e.getMessage());
                    System.err.println("WARNING: This might be due to unsupported file format or invalid URL");
                    isMusicLoading = false;
                    return;  // Continue without music
                }
                
                // Set up error handler BEFORE creating MediaPlayer
                media.setOnError(() -> {
                    if (media.getError() != null) {
                        System.err.println("WARNING: Media error: " + media.getError().getMessage());
                    }
                });
                
                // Create MediaPlayer with error handling
                MediaPlayer player;
                try {
                    player = new MediaPlayer(media);
                    backgroundMusic = player;
                } catch (Exception e) {
                    System.err.println("WARNING: Could not create MediaPlayer: " + e.getMessage());
                    backgroundMedia = null;
                    isMusicLoading = false;
                    return;  // Continue without music
                }
                
                // Configure MediaPlayer
                try {
                    player.setVolume(masterVolume);
                    player.setCycleCount(MediaPlayer.INDEFINITE);  // Loop forever
                } catch (Exception e) {
                    System.err.println("WARNING: Could not configure MediaPlayer: " + e.getMessage());
                    // Continue - these are non-critical
                }
                
                // Set up error handling for MediaPlayer
                player.setOnError(() -> {
                    if (player.getError() != null) {
                        System.err.println("WARNING: Music player error: " + player.getError().getMessage());
                    }
                });
                
                // Wait for media to be ready before playing
                player.setOnReady(() -> {
                    System.out.println("INFO: Background music ready!");
                    if (musicEnabled) {
                        try {
                            player.play();
                            System.out.println("INFO: Background music started");
                        } catch (Exception e) {
                            System.err.println("WARNING: Error playing music when ready: " + e.getMessage());
                        }
                    }
                });
                
                System.out.println("INFO: Background music loading initiated");
                
            } catch (Exception e) {
                System.err.println("WARNING: Unexpected error loading background music: " + e.getMessage());
                e.printStackTrace();
                // Continue without music - don't break the game
            } finally {
                isMusicLoading = false;
            }
        });
    }
    
    /**
     * Stops background music and releases media resources safely.
     * This method is thread-safe and UI-safe.
     */
    public void stopBackgroundMusic() {
        stopMusic();
    }
    
    /**
     * Stops background music and releases media resources safely.
     * This method is thread-safe and UI-safe.
     * Use this when leaving the main menu or shutting down the game.
     */
    public void stopMusic() {
        try {
            if (backgroundMusic != null) {
                if (javafx.application.Platform.isFxApplicationThread()) {
                    backgroundMusic.stop();
                    backgroundMusic.dispose();
                    backgroundMusic = null;
                } else {
                    javafx.application.Platform.runLater(() -> {
                        if (backgroundMusic != null) {
                            try {
                                backgroundMusic.stop();
                                backgroundMusic.dispose();
                            } catch (Exception e) {
                                System.err.println("WARNING: Error disposing background music: " + e.getMessage());
                            }
                            backgroundMusic = null;
                        }
                    });
                }
            }
            backgroundMedia = null;
            isMusicLoading = false;
            System.out.println("INFO: Background music stopped and resources released");
        } catch (Exception e) {
            System.err.println("WARNING: Error stopping background music: " + e.getMessage());
            // Continue - cleanup failure shouldn't break the app
        }
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
                Media media;
                try {
                    media = new Media(mediaUrl);
                } catch (Exception e) {
                    // Silently fail - invalid media format
                    return;
                }
                
                // Set up error handler
                media.setOnError(() -> {
                    // Silently fail on media error
                });
                
                // Create MediaPlayer with error handling
                MediaPlayer soundPlayer;
                try {
                    soundPlayer = new MediaPlayer(media);
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

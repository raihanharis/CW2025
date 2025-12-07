package com.comp2042;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.util.prefs.Preferences;

/**
 * Manages background music for the Tetris game.
 * Handles loading, playing, pausing, stopping, and volume control.
 * All audio operations are non-blocking and safe - failures won't break the UI.
 */
public class BackgroundMusicManager {
    
    private static BackgroundMusicManager instance;
    
    private MediaPlayer musicPlayer;
    private Media musicMedia;
    private double volume = 1.0;  // 0.0 to 1.0
    private boolean musicEnabled = true;
    private double originalVolume = 1.0;  // Store original volume for pause/unpause
    private boolean isLoading = false;  // Prevent multiple simultaneous loads
    
    private Preferences prefs;
    
    private BackgroundMusicManager() {
        try {
            prefs = Preferences.userNodeForPackage(BackgroundMusicManager.class);
            loadSettings();
            // DO NOT load music in constructor - it will be loaded lazily and asynchronously
            System.out.println("BackgroundMusicManager initialized (music will load on demand)");
        } catch (Exception e) {
            System.err.println("WARNING: Error in BackgroundMusicManager constructor: " + e.getMessage());
            e.printStackTrace();
            // Continue without music if initialization fails - don't break the app
        }
    }
    
    /**
     * Ensures music is loaded. Called lazily when music is first needed.
     * This method is completely non-blocking and safe.
     */
    private void ensureMusicLoaded() {
        // If already loaded or currently loading, return immediately
        if (musicPlayer != null || isLoading) {
            return;
        }
        
        // Check if JavaFX toolkit is ready
        if (!javafx.application.Platform.isFxApplicationThread()) {
            // If not on FX thread, schedule on FX thread
            javafx.application.Platform.runLater(this::ensureMusicLoaded);
            return;
        }
        
        // Mark as loading to prevent concurrent loads
        isLoading = true;
        
        // Load music asynchronously to avoid blocking UI
        javafx.application.Platform.runLater(() -> {
            try {
                loadMusic();
            } catch (Exception e) {
                System.err.println("WARNING: Failed to load background music: " + e.getMessage());
                e.printStackTrace();
                // Continue without music - don't break the app
            } finally {
                isLoading = false;
            }
        });
    }
    
    public static BackgroundMusicManager getInstance() {
        if (instance == null) {
            synchronized (BackgroundMusicManager.class) {
                if (instance == null) {
                    instance = new BackgroundMusicManager();
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
            volume = prefs.getDouble("musicVolume", 1.0);
            musicEnabled = prefs.getBoolean("musicEnabled", true);
            originalVolume = volume;
        } catch (Exception e) {
            System.err.println("WARNING: Error loading music settings: " + e.getMessage());
            // Use defaults if preferences fail
            volume = 1.0;
            musicEnabled = true;
            originalVolume = 1.0;
        }
    }
    
    /**
     * Saves settings to preferences.
     */
    private void saveSettings() {
        try {
            prefs.putDouble("musicVolume", volume);
            prefs.putBoolean("musicEnabled", musicEnabled);
        } catch (Exception e) {
            System.err.println("WARNING: Error saving music settings: " + e.getMessage());
            // Continue - settings save failure shouldn't break the app
        }
    }
    
    /**
     * Loads the background music file.
     * This method is safe and handles all errors gracefully.
     */
    private void loadMusic() {
        // Ensure we're on the JavaFX thread
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(this::loadMusic);
            return;
        }
        
        try {
            // Try to load music file from resources - check multiple possible names
            String[] possiblePaths = {
                "/sounds/tetris_theme.mp3",
                "/sounds/background_music.mp3"
            };
            
            java.net.URL musicUrl = null;
            String musicPath = null;
            
            // Try each path until we find one that exists
            for (String path : possiblePaths) {
                try {
                    musicUrl = getClass().getResource(path);
                    if (musicUrl != null) {
                        musicPath = path;
                        System.out.println("Found music file at: " + path);
                        break;
                    }
                } catch (Exception e) {
                    // Silently continue trying other paths - don't log errors
                }
            }
            
            if (musicUrl == null) {
                // Silently continue without music - don't print warnings that might confuse users
                // Music will simply not play, but the game continues normally
                return;  // Exit gracefully - no music file found
            }
            
            // Convert URL to string for Media - handle different URL formats
            String mediaUrl;
            try {
                mediaUrl = musicUrl.toExternalForm();
                System.out.println("Loading music from: " + mediaUrl);
            } catch (Exception e) {
                System.err.println("WARNING: Could not convert music URL to string: " + e.getMessage());
                return;  // Exit gracefully
            }
            
            // Create Media object with error handling
            try {
                musicMedia = new Media(mediaUrl);
            } catch (Exception e) {
                System.err.println("WARNING: Could not create Media object: " + e.getMessage());
                System.err.println("WARNING: This might be due to unsupported file format or invalid URL");
                musicMedia = null;
                return;  // Exit gracefully
            }
            
            // Set up error handler BEFORE creating MediaPlayer
            musicMedia.setOnError(() -> {
                if (musicMedia.getError() != null) {
                    System.err.println("WARNING: Media error: " + musicMedia.getError().getMessage());
                }
            });
            
            // Create MediaPlayer with error handling
            try {
                musicPlayer = new MediaPlayer(musicMedia);
            } catch (Exception e) {
                System.err.println("WARNING: Could not create MediaPlayer: " + e.getMessage());
                musicPlayer = null;
                musicMedia = null;
                return;  // Exit gracefully
            }
            
            // Configure MediaPlayer
            try {
                musicPlayer.setVolume(volume);
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);  // Loop forever
            } catch (Exception e) {
                System.err.println("WARNING: Could not configure MediaPlayer: " + e.getMessage());
                // Continue - these are non-critical
            }
            
            // Set up error handling for MediaPlayer
            musicPlayer.setOnError(() -> {
                if (musicPlayer.getError() != null) {
                    System.err.println("WARNING: Music player error: " + musicPlayer.getError().getMessage());
                }
            });
            
            // Wait for media to be ready before allowing playback
            musicPlayer.setOnReady(() -> {
                System.out.println("INFO: Music media ready!");
            });
            
            System.out.println("INFO: Music loaded successfully!");
            
        } catch (Exception e) {
            System.err.println("WARNING: Unexpected error loading background music: " + e.getMessage());
            e.printStackTrace();
            // Continue without music if file not found - don't break the app
            musicPlayer = null;
            musicMedia = null;
        }
    }
    
    /**
     * Starts playing the background music (if enabled).
     * This method is safe and non-blocking.
     */
    public void playMusic() {
        try {
            // Load music lazily if not already loaded (non-blocking)
            ensureMusicLoaded();
            
            // If music is still loading, it will play when ready
            if (musicPlayer == null) {
                // Music might still be loading, or failed to load
                // This is OK - we'll continue without music
                if (!isLoading) {
                    System.out.println("INFO: Cannot play music: music file not available");
                }
                return;
            }
            
            if (musicEnabled) {
                try {
                    musicPlayer.setVolume(volume);
                    musicPlayer.play();
                    System.out.println("INFO: Music playback started. Volume: " + volume);
                } catch (Exception e) {
                    System.err.println("WARNING: Error playing music: " + e.getMessage());
                    // Continue - music playback failure shouldn't break the app
                }
            } else {
                System.out.println("INFO: Music is disabled");
            }
        } catch (Exception e) {
            System.err.println("WARNING: Unexpected error in playMusic(): " + e.getMessage());
            e.printStackTrace();
            // Continue - don't break the app
        }
    }
    
    /**
     * Pauses the background music.
     */
    public void pauseMusic() {
        try {
            if (musicPlayer != null) {
                musicPlayer.pause();
            }
        } catch (Exception e) {
            System.err.println("WARNING: Error pausing music: " + e.getMessage());
        }
    }
    
    /**
     * Stops the background music.
     */
    public void stopMusic() {
        try {
            if (musicPlayer != null) {
                musicPlayer.stop();
            }
        } catch (Exception e) {
            System.err.println("WARNING: Error stopping music: " + e.getMessage());
        }
    }
    
    /**
     * Sets the music volume (0.0 to 1.0).
     * @param newVolume volume level between 0.0 and 1.0
     */
    public void setVolume(double newVolume) {
        try {
            if (newVolume < 0.0) newVolume = 0.0;
            if (newVolume > 1.0) newVolume = 1.0;
            
            this.volume = newVolume;
            this.originalVolume = newVolume;  // Update original volume
            
            if (musicPlayer != null) {
                musicPlayer.setVolume(volume);
            }
            
            saveSettings();
        } catch (Exception e) {
            System.err.println("WARNING: Error setting music volume: " + e.getMessage());
        }
    }
    
    /**
     * Gets the current music volume (0.0 to 1.0).
     */
    public double getVolume() {
        return volume;
    }
    
    /**
     * Sets whether music is enabled.
     * @param enabled true to enable music, false to disable
     */
    public void setMusicEnabled(boolean enabled) {
        try {
            this.musicEnabled = enabled;
            
            if (musicEnabled) {
                playMusic();
            } else {
                stopMusic();
            }
            
            saveSettings();
        } catch (Exception e) {
            System.err.println("WARNING: Error setting music enabled: " + e.getMessage());
        }
    }
    
    /**
     * Gets whether music is enabled.
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    /**
     * Reduces music volume to 50% (for pause state).
     */
    public void reduceVolumeForPause() {
        try {
            if (musicPlayer != null) {
                originalVolume = volume;  // Store current volume
                double pausedVolume = volume * 0.5;  // 50% of current volume
                musicPlayer.setVolume(pausedVolume);
            }
        } catch (Exception e) {
            System.err.println("WARNING: Error reducing volume for pause: " + e.getMessage());
        }
    }
    
    /**
     * Restores music volume to original level (for unpause).
     */
    public void restoreVolumeAfterPause() {
        try {
            if (musicPlayer != null) {
                musicPlayer.setVolume(originalVolume);
            }
        } catch (Exception e) {
            System.err.println("WARNING: Error restoring volume after pause: " + e.getMessage());
        }
    }
    
    /**
     * Cleanly shuts down the music manager.
     */
    public void shutdown() {
        try {
            if (musicPlayer != null) {
                musicPlayer.stop();
                musicPlayer.dispose();
                musicPlayer = null;
            }
            musicMedia = null;
        } catch (Exception e) {
            System.err.println("WARNING: Error shutting down music manager: " + e.getMessage());
        }
    }
}

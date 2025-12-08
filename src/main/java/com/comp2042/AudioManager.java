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
            prefs.putBoolean("musicEnabled", musicEnabled);
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
     * Starts background music (if music is enabled).
     * Music will loop continuously.
     * This method is safe, non-blocking, and will only create one MediaPlayer instance.
     * If called multiple times, it reuses the existing MediaPlayer.
     */
    public void startBackgroundMusic(String musicPath) {
        System.out.println("========================================");
        System.out.println("startBackgroundMusic() called with path: " + musicPath);
        System.out.println("Music enabled: " + musicEnabled);
        System.out.println("Master volume: " + masterVolume);
        System.out.println("========================================");
        
        // Check if music is enabled first
        if (!musicEnabled) {
            System.out.println("WARNING: Music is disabled in settings!");
            System.out.println("WARNING: Go to Settings → MUSIC → Turn ON to enable music");
            return;
        }
        
        // If already loading, wait for it to finish
        if (isMusicLoading) {
            System.out.println("INFO: Music is already loading, will play when ready");
            return;
        }
        
        // If already loaded, check status and play if needed
        if (backgroundMusic != null) {
            System.out.println("INFO: Background music already loaded, checking status...");
            try {
                MediaPlayer.Status status = backgroundMusic.getStatus();
                System.out.println("INFO: Current MediaPlayer status: " + status);
                
                if (status == MediaPlayer.Status.DISPOSED || status == MediaPlayer.Status.UNKNOWN) {
                    // MediaPlayer was disposed, need to reload
                    System.out.println("INFO: MediaPlayer was disposed, reloading music...");
                    backgroundMusic = null;
                    backgroundMedia = null;
                    // Fall through to load new music
                } else if (status == MediaPlayer.Status.PLAYING) {
                    System.out.println("INFO: Background music is already playing");
                    return;
                } else {
                    // MediaPlayer exists and is valid, just play it
                    if (javafx.application.Platform.isFxApplicationThread()) {
                        backgroundMusic.setVolume(masterVolume);
                        backgroundMusic.play();
                        System.out.println("INFO: Playing existing background music (status: " + backgroundMusic.getStatus() + ")");
                    } else {
                        javafx.application.Platform.runLater(() -> {
                            if (backgroundMusic != null && musicEnabled) {
                                backgroundMusic.setVolume(masterVolume);
                                backgroundMusic.play();
                                System.out.println("INFO: Playing existing background music (status: " + backgroundMusic.getStatus() + ")");
                            }
                        });
                    }
                    return;  // Don't reload if MediaPlayer is valid
                }
            } catch (Exception e) {
                System.err.println("ERROR: Error playing existing background music: " + e.getMessage());
                e.printStackTrace();
                // Try to reload
                backgroundMusic = null;
                backgroundMedia = null;
                // Fall through to load new music
            }
        }
        
        // Mark as loading
        isMusicLoading = true;
        
        // Load music asynchronously on JavaFX thread to avoid blocking UI
        // Make musicPath final for lambda
        final String finalMusicPath = musicPath;
        javafx.application.Platform.runLater(() -> {
            try {
                System.out.println("INFO: Starting to load music from: " + finalMusicPath);
                
                // Check for null resource before creating Media object
                java.net.URL musicUrl = null;
                String actualPath = finalMusicPath;
                
                try {
                    musicUrl = getClass().getResource(finalMusicPath);
                    System.out.println("INFO: Resource lookup result: " + (musicUrl != null ? musicUrl.toString() : "NULL"));
                } catch (Exception e) {
                    System.err.println("ERROR: Error getting music resource: " + e.getMessage());
                    e.printStackTrace();
                }
                
                // Try multiple file formats (MP3, WAV, etc.)
                if (musicUrl == null) {
                    // Try MP3 version if WAV not found
                    String mp3Path = finalMusicPath.replace(".wav", ".mp3");
                    if (!mp3Path.equals(finalMusicPath)) {
                        System.out.println("INFO: WAV file not found, trying MP3: " + mp3Path);
                        musicUrl = getClass().getResource(mp3Path);
                        if (musicUrl != null) {
                            actualPath = mp3Path;  // Use MP3 path
                            System.out.println("INFO: Found MP3 file instead!");
                        }
                    }
                }
                
                if (musicUrl == null) {
                    System.err.println("ERROR: Background music file not found!");
                    System.err.println("ERROR: Tried: " + finalMusicPath);
                    if (finalMusicPath.endsWith(".wav")) {
                        System.err.println("ERROR: Also tried: " + finalMusicPath.replace(".wav", ".mp3"));
                    }
                    System.err.println("ERROR: Please ensure the file exists at: src/main/resources/sounds/");
                    System.err.println("ERROR: Supported formats: .mp3 or .wav");
                    System.err.println("ERROR: Game will continue without background music.");
                    isMusicLoading = false;
                    return;  // Continue without music - don't break the game
                }
                
                System.out.println("INFO: Music file found at: " + musicUrl);
                
                // Convert URL to string format that Media can use
                String mediaUrlString;
                try {
                    mediaUrlString = musicUrl.toExternalForm();
                    System.out.println("INFO: Converted URL to string: " + mediaUrlString);
                } catch (Exception e) {
                    System.err.println("ERROR: Error converting music URL to string: " + e.getMessage());
                    e.printStackTrace();
                    isMusicLoading = false;
                    return;
                }
                
                // Create Media object with error handling
                System.out.println("INFO: Creating Media object from: " + mediaUrlString);
                
                Media media;
                try {
                    // Use the string URL instead of URL object to avoid module access issues
                    media = new Media(mediaUrlString);
                    backgroundMedia = media;
                    System.out.println("INFO: Media object created successfully");
                } catch (IllegalAccessError e) {
                    System.err.println("========================================");
                    System.err.println("ERROR: JavaFX Media module access error!");
                    System.err.println("ERROR: This is a JavaFX module system issue.");
                    System.err.println("ERROR: Possible solutions:");
                    System.err.println("  1. Try converting your WAV file to MP3 format");
                    System.err.println("  2. Add VM arguments: --add-opens javafx.media/javafx.scene.media=ALL-UNNAMED");
                    System.err.println("  3. Ensure JavaFX media module is properly included");
                    System.err.println("========================================");
                    e.printStackTrace();
                    isMusicLoading = false;
                    return;  // Continue without music
                } catch (Exception e) {
                    System.err.println("ERROR: Could not create Media object: " + e.getMessage());
                    System.err.println("ERROR: Exception type: " + e.getClass().getName());
                    System.err.println("ERROR: This might be due to:");
                    System.err.println("  - Unsupported file format");
                    System.err.println("  - Missing codec for WAV files");
                    System.err.println("  - Try converting to MP3 format");
                    e.printStackTrace();
                    isMusicLoading = false;
                    return;  // Continue without music
                }
                
                // Set up error handler BEFORE creating MediaPlayer
                media.setOnError(() -> {
                    if (media.getError() != null) {
                        System.err.println("ERROR: Media error: " + media.getError().getMessage());
                        media.getError().printStackTrace();
                    } else {
                        System.err.println("ERROR: Media error occurred (no error details)");
                    }
                    isMusicLoading = false;
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
                    System.out.println("INFO: MediaPlayer configured - volume: " + masterVolume + ", loop: INDEFINITE");
                } catch (Exception e) {
                    System.err.println("ERROR: Could not configure MediaPlayer: " + e.getMessage());
                    e.printStackTrace();
                    // Continue - these are non-critical
                }
                
                // Set up error handling for MediaPlayer
                player.setOnError(() -> {
                    if (player.getError() != null) {
                        System.err.println("ERROR: MediaPlayer error: " + player.getError().getMessage());
                        player.getError().printStackTrace();
                    } else {
                        System.err.println("ERROR: MediaPlayer error occurred (no error details)");
                    }
                    isMusicLoading = false;
                });
                
                // Add status change listener for debugging
                player.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                    System.out.println("INFO: MediaPlayer status changed: " + oldStatus + " -> " + newStatus);
                    if (newStatus == MediaPlayer.Status.PLAYING) {
                        System.out.println("INFO: ✓✓✓ Background music is NOW PLAYING! ✓✓✓");
                        isMusicLoading = false;
                    } else if (newStatus == MediaPlayer.Status.STOPPED || newStatus == MediaPlayer.Status.HALTED) {
                        System.err.println("WARNING: MediaPlayer stopped unexpectedly. Status: " + newStatus);
                    }
                });
                
                // Wait for media to be ready before playing
                player.setOnReady(() -> {
                    System.out.println("INFO: Background music ready!");
                    System.out.println("INFO: Music enabled: " + musicEnabled);
                    System.out.println("INFO: Master volume: " + masterVolume);
                    System.out.println("INFO: MediaPlayer status: " + player.getStatus());
                    
                    if (!musicEnabled) {
                        System.out.println("WARNING: Music is disabled in settings!");
                        System.out.println("WARNING: Go to Settings → MUSIC → Turn ON to enable music");
                        isMusicLoading = false;
                        return;
                    }
                    
                    try {
                        // Ensure volume is set (even if 0, set it explicitly)
                        double vol = Math.max(0.0, Math.min(1.0, masterVolume));
                        player.setVolume(vol);
                        System.out.println("INFO: Volume set to: " + vol);
                        
                        if (vol <= 0.0) {
                            System.err.println("WARNING: Master volume is 0! Music will be silent.");
                            System.err.println("WARNING: Increase Master Volume in Settings to hear music.");
                        }
                        
                        // Play the music immediately
                        System.out.println("INFO: Calling player.play()...");
                        player.play();
                        
                        // Check status immediately
                        MediaPlayer.Status immediateStatus = player.getStatus();
                        System.out.println("INFO: Immediate status after play(): " + immediateStatus);
                        
                        // Also check after a delay to ensure it started
                        javafx.application.Platform.runLater(() -> {
                            javafx.application.Platform.runLater(() -> {
                                try {
                                    MediaPlayer.Status status = player.getStatus();
                                    System.out.println("INFO: Delayed status check: " + status);
                                    
                                    if (status == MediaPlayer.Status.PLAYING) {
                                        System.out.println("========================================");
                                        System.out.println("✓✓✓ SUCCESS: Background music is PLAYING! ✓✓✓");
                                        System.out.println("========================================");
                                        isMusicLoading = false;
                                    } else if (status == MediaPlayer.Status.READY || status == MediaPlayer.Status.STOPPED) {
                                        System.err.println("WARNING: MediaPlayer is " + status + " but not PLAYING");
                                        System.err.println("WARNING: Attempting to play again...");
                                        try {
                                            player.play();
                                            javafx.application.Platform.runLater(() -> {
                                                MediaPlayer.Status status2 = player.getStatus();
                                                System.out.println("INFO: After retry, status: " + status2);
                                                if (status2 == MediaPlayer.Status.PLAYING) {
                                                    System.out.println("✓✓✓ SUCCESS: Music is NOW PLAYING after retry! ✓✓✓");
                                                } else {
                                                    System.err.println("========================================");
                                                    System.err.println("ERROR: Music failed to start!");
                                                    System.err.println("ERROR: Status: " + status2);
                                                    System.err.println("ERROR: Possible causes:");
                                                    System.err.println("  1. WAV file codec not supported");
                                                    System.err.println("  2. File is corrupted");
                                                    System.err.println("  3. Try converting to MP3 format");
                                                    System.err.println("========================================");
                                                }
                                                isMusicLoading = false;
                                            });
                                        } catch (Exception retryEx) {
                                            System.err.println("ERROR: Retry failed: " + retryEx.getMessage());
                                            retryEx.printStackTrace();
                                            isMusicLoading = false;
                                        }
                                    } else {
                                        System.err.println("ERROR: Unexpected status: " + status);
                                        isMusicLoading = false;
                                    }
                                } catch (Exception e2) {
                                    System.err.println("ERROR: Exception in delayed check: " + e2.getMessage());
                                    e2.printStackTrace();
                                    isMusicLoading = false;
                                }
                            });
                        });
                    } catch (Exception e) {
                        System.err.println("ERROR: Exception playing music: " + e.getMessage());
                        e.printStackTrace();
                        isMusicLoading = false;
                    }
                });
                
                // Handle end of media (should loop automatically with INDEFINITE, but log it)
                player.setOnEndOfMedia(() -> {
                    System.out.println("INFO: Music reached end - should loop automatically");
                });
                
                System.out.println("INFO: Background music loading initiated");
                
            } catch (Exception e) {
                System.err.println("ERROR: Unexpected error loading background music: " + e.getMessage());
                e.printStackTrace();
                isMusicLoading = false;
                // Continue without music - don't break the game
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

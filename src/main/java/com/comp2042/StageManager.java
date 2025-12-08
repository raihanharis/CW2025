package com.comp2042;

import javafx.stage.Stage;

/**
 * Global Stage manager to prevent window flicker and minimization.
 * All controllers should use this to get the primary stage instead of
 * creating new ones or modifying stage properties.
 */
public class StageManager {
    private static Stage primaryStage;
    
    /**
     * Sets the primary stage. Should only be called once from Main.java.
     */
    public static void setPrimaryStage(Stage stage) {
        if (primaryStage == null) {
            primaryStage = stage;
            
            // Add listener to prevent accidental minimization - execute IMMEDIATELY
            primaryStage.iconifiedProperty().addListener((obs, wasIconified, isIconified) -> {
                if (isIconified) {
                    // IMMEDIATELY restore if minimized - execute on current thread if possible
                    if (javafx.application.Platform.isFxApplicationThread()) {
                        if (primaryStage != null && primaryStage.isIconified()) {
                            primaryStage.setIconified(false);
                            primaryStage.toFront();
                        }
                    } else {
                        javafx.application.Platform.runLater(() -> {
                            if (primaryStage != null && primaryStage.isIconified()) {
                                primaryStage.setIconified(false);
                                primaryStage.toFront();
                            }
                        });
                    }
                }
            });
            
            // Also prevent window from losing focus during transitions
            primaryStage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused && primaryStage.isFullScreen()) {
                    // Keep fullscreen window focused
                    javafx.application.Platform.runLater(() -> {
                        if (primaryStage != null && !primaryStage.isFocused()) {
                            primaryStage.requestFocus();
                        }
                    });
                }
            });
        }
    }
    
    /**
     * Gets the primary stage. Returns null if not set.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Instantly switches root nodes in the SINGLE Scene without any flicker.
     * This is the macOS-compatible approach - never replaces the Scene, only swaps root.
     * Preserves fullscreen/maximized state completely.
     * 
     * Requirements:
     * - NEVER calls setScene() after startup
     * - ONLY calls scene.setRoot() to swap root nodes
     * - NEVER modifies window size, position, or geometry
     * - NEVER calls hide(), show(), or creates new Stage
     * - Uses preloaded root nodes for instant switching
     * - Executes IMMEDIATELY on JavaFX thread (no Platform.runLater delay)
     */
    public static void switchRoot(javafx.scene.Parent newRoot, String title) {
        if (primaryStage == null) {
            System.err.println("ERROR: Primary stage not set in StageManager!");
            return;
        }
        
        javafx.scene.Scene singleScene = SceneManager.getSingleScene();
        if (singleScene == null) {
            System.err.println("ERROR: Single scene not created!");
            return;
        }
        
        // Execute IMMEDIATELY if on JavaFX thread, otherwise use Platform.runLater
        Runnable swapRoot = () -> {
            try {
                // IMMEDIATELY prevent minimization BEFORE swap
                if (primaryStage.isIconified()) {
                    primaryStage.setIconified(false);
                }
                
                // Ensure window is showing and on top
                if (!primaryStage.isShowing()) {
                    primaryStage.show();
                }
                primaryStage.toFront();
                
                // ONLY swap root node and update title - nothing else
                // This is the key fix for macOS - swapping root doesn't recreate the window
                singleScene.setRoot(newRoot);
                if (title != null) {
                    primaryStage.setTitle(title);
                }
                
                // IMMEDIATELY ensure not minimized AFTER swap
                if (primaryStage.isIconified()) {
                    primaryStage.setIconified(false);
                }
                primaryStage.toFront();
                
                // Request focus for keyboard input
                if (newRoot != null) {
                    newRoot.requestFocus();
                }
                
            } catch (Exception e) {
                System.err.println("ERROR switching root: " + e.getMessage());
                e.printStackTrace();
            }
        };
        
        // Execute immediately if on JavaFX thread, otherwise schedule
        if (javafx.application.Platform.isFxApplicationThread()) {
            swapRoot.run();
        } else {
            javafx.application.Platform.runLater(swapRoot);
        }
    }
    
    /**
     * Legacy method for game scene (which needs to be created dynamically).
     * Still uses root swapping instead of scene replacement.
     */
    public static void switchToGameScene(javafx.scene.Scene gameScene, String title) {
        // For game scene, we still need to create it, but swap the root
        if (primaryStage == null || gameScene == null) {
            System.err.println("ERROR: Stage or scene is null!");
            return;
        }
        
        javafx.scene.Parent gameRoot = gameScene.getRoot();
        if (gameRoot != null) {
            // Fix root pane sizing
            SceneManager.fixRootPaneSizingExternal(gameRoot);
            // Use root swapping instead of scene replacement
            switchRoot(gameRoot, title);
        }
    }
}


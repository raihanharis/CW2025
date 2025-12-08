package com.comp2042;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.util.HashMap;
import java.util.Map;

/**
 * Preloads and manages all FXML root nodes at startup to prevent flicker.
 * Uses a SINGLE Scene and swaps root nodes instead of replacing scenes.
 * This prevents macOS from recreating the window and keeps fullscreen stable.
 */
public class SceneManager {
    private static final Map<String, Parent> preloadedRoots = new HashMap<>();
    private static final Map<String, Object> preloadedControllers = new HashMap<>();
    private static Scene singleScene;
    
    /**
     * Preloads ALL FXML root nodes at application startup.
     * This must be called from Main.java before showing the stage.
     */
    public static void preloadAllScenes() {
        try {
            // Preload Main Menu
            FXMLLoader mainMenuLoader = new FXMLLoader(
                    SceneManager.class.getResource("/mainMenu.fxml")
            );
            Parent mainMenuRoot = mainMenuLoader.load();
            MainMenuController mainMenuController = mainMenuLoader.getController();
            
            // Fix root pane sizing to prevent auto-resizing
            fixRootPaneSizing(mainMenuRoot);
            
            preloadedRoots.put("mainMenu", mainMenuRoot);
            preloadedControllers.put("mainMenu", mainMenuController);
            
            // Preload Settings
            FXMLLoader settingsLoader = new FXMLLoader(
                    SceneManager.class.getResource("/settings.fxml")
            );
            Parent settingsRoot = settingsLoader.load();
            SettingsController settingsController = settingsLoader.getController();
            
            // Fix root pane sizing to prevent auto-resizing
            fixRootPaneSizing(settingsRoot);
            
            preloadedRoots.put("settings", settingsRoot);
            preloadedControllers.put("settings", settingsController);
            
            System.out.println("All FXML roots preloaded successfully!");
        } catch (Exception e) {
            System.err.println("ERROR preloading scenes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates the single Scene that will be used for the entire application.
     * This Scene is never replaced - only its root node is swapped.
     */
    public static Scene createSingleScene(Parent initialRoot) {
        if (singleScene == null) {
            singleScene = new Scene(initialRoot, 900, 700);
            singleScene.setFill(javafx.scene.paint.Color.web("#000000"));
        }
        return singleScene;
    }
    
    /**
     * Gets the single Scene used for the entire application.
     */
    public static Scene getSingleScene() {
        return singleScene;
    }
    
    /**
     * Gets a preloaded root node by name.
     * Returns null if not found.
     */
    public static Parent getPreloadedRoot(String rootName) {
        return preloadedRoots.get(rootName);
    }
    
    /**
     * Gets a preloaded controller by scene name.
     * Returns null if not found.
     */
    public static Object getPreloadedController(String sceneName) {
        return preloadedControllers.get(sceneName);
    }
    
    /**
     * Gets the preloaded MainMenuController.
     */
    public static MainMenuController getMainMenuController() {
        return (MainMenuController) preloadedControllers.get("mainMenu");
    }
    
    /**
     * Gets the preloaded SettingsController.
     */
    public static SettingsController getSettingsController() {
        return (SettingsController) preloadedControllers.get("settings");
    }
    
    /**
     * Fixes root pane sizing to prevent automatic resizing.
     * Must be called on all root panes after loading.
     */
    private static void fixRootPaneSizing(Parent root) {
        if (root instanceof javafx.scene.layout.Region) {
            javafx.scene.layout.Region region = (javafx.scene.layout.Region) root;
            region.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
            region.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
            // Prevent any layout recalculation
            region.setManaged(true);
        }
    }
    
    /**
     * Fixes root pane sizing for externally loaded roots (like game layout).
     * Public method to be called from controllers.
     */
    public static void fixRootPaneSizingExternal(Parent root) {
        if (root instanceof javafx.scene.layout.Region) {
            javafx.scene.layout.Region region = (javafx.scene.layout.Region) root;
            region.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
            region.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
            // Prevent any layout recalculation
            region.setManaged(true);
        }
    }
}


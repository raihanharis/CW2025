package com.comp2042;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Defines different visual skins/themes for the Tetris game.
 * Each skin provides a color scheme for all 7 brick types.
 */
public enum Skin {
    CLASSIC("Classic", 
        Color.CYAN,           // I-piece
        Color.BLUEVIOLET,     // J-piece
        Color.LIMEGREEN,      // L-piece
        Color.YELLOW,         // O-piece
        Color.RED,            // S-piece
        Color.ORANGE,         // T-piece
        Color.DEEPSKYBLUE     // Z-piece
    ),
    
    NEON("Neon",
        Color.web("#00FFFF"),      // I-piece - Bright Cyan
        Color.web("#FF00FF"),      // J-piece - Magenta
        Color.web("#00FF00"),      // L-piece - Bright Green
        Color.web("#FFFF00"),      // O-piece - Bright Yellow
        Color.web("#FF0080"),      // S-piece - Hot Pink
        Color.web("#FF8000"),      // T-piece - Bright Orange
        Color.web("#0080FF")       // Z-piece - Bright Blue
    ),
    
    RETRO("Retro",
        Color.web("#00D9FF"),      // I-piece - Light Blue
        Color.web("#0000FF"),      // J-piece - Blue
        Color.web("#FFA500"),      // L-piece - Orange
        Color.web("#FFFF00"),      // O-piece - Yellow
        Color.web("#00FF00"),      // S-piece - Green
        Color.web("#800080"),      // T-piece - Purple
        Color.web("#FF0000")       // Z-piece - Red
    ),
    
    PASTEL("Pastel",
        Color.web("#B3E5FC"),      // I-piece - Light Blue
        Color.web("#C5CAE9"),      // J-piece - Lavender
        Color.web("#C8E6C9"),      // L-piece - Light Green
        Color.web("#FFF9C4"),      // O-piece - Light Yellow
        Color.web("#F8BBD0"),      // S-piece - Pink
        Color.web("#FFCCBC"),      // T-piece - Peach
        Color.web("#B2DFDB")       // Z-piece - Teal
    ),
    
    DARK("Dark",
        Color.web("#4FC3F7"),      // I-piece - Light Blue
        Color.web("#7B1FA2"),      // J-piece - Purple
        Color.web("#66BB6A"),      // L-piece - Green
        Color.web("#FFA726"),      // O-piece - Orange
        Color.web("#EF5350"),      // S-piece - Red
        Color.web("#FF7043"),      // T-piece - Deep Orange
        Color.web("#42A5F5")       // Z-piece - Blue
    );
    
    private final String displayName;
    private final Color[] colors;  // Array of 7 colors for the 7 brick types
    
    Skin(String displayName, Color color1, Color color2, Color color3, Color color4, 
         Color color5, Color color6, Color color7) {
        this.displayName = displayName;
        this.colors = new Color[]{
            color1, color2, color3, color4, color5, color6, color7
        };
    }
    
    /**
     * Gets the display name of this skin.
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the color for a specific brick type.
     * @param brickValue The brick value (1-7)
     * @return The color for that brick type
     */
    public Paint getColor(int brickValue) {
        if (brickValue >= 1 && brickValue <= 7) {
            return colors[brickValue - 1];
        }
        return Color.TRANSPARENT;
    }
    
    /**
     * Gets a semi-transparent version of the color for the ghost piece.
     * @param brickValue The brick value (1-7)
     * @return A semi-transparent color for the ghost piece
     */
    public Paint getGhostColor(int brickValue) {
        if (brickValue >= 1 && brickValue <= 7) {
            return colors[brickValue - 1].deriveColor(0, 1, 1, 0.25);
        }
        return Color.TRANSPARENT;
    }
    
    /**
     * Gets all available skins.
     */
    public static Skin[] getAllSkins() {
        return values();
    }
}


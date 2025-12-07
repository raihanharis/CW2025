package com.comp2042;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Represents the player's score in the Tetris game.
 * Uses official Tetris Guideline base scoring for line clears:
 * - Single (1 line): 100 points
 * - Double (2 lines): 300 points
 * - Triple (3 lines): 500 points
 * - Tetris (4 lines): 800 points
 *
 * Uses JavaFX's {@link IntegerProperty} so the GUI can automatically update
 * whenever the score changes (data binding).
 *
 * Responsibilities:
 * - Store the current score
 * - Allow controlled scoring updates (adds points directly, no formulas)
 * - Provide a reset mechanism for new games
 * - Expose a bindable property for the GUI
 */
public final class Score {

    /** The player's current score, stored as a JavaFX bindable property. */
    private final IntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * Provides access to the underlying score property.
     * This allows UI elements to bind to the score and update automatically.
     *
     * @return the IntegerProperty representing the score
     */
    public IntegerProperty scoreProperty() {
        return score;
    }

    /**
     * Adds points to the current score.
     *
     * @param pointsToAdd number of points to add (must be >= 0)
     */
    public void add(int pointsToAdd) {
        score.set(score.get() + pointsToAdd);
    }

    /**
     * Resets the score back to zero.
     * Called when a new game is started.
     */
    public void reset() {
        score.set(0);
    }
}

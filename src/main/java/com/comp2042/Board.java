package com.comp2042;

/**
 * Represents the main game board in the Tetris application.
 *
 * This interface defines the essential operations required to:
 * - Move and rotate the active brick
 * - Merge bricks into the background matrix
 * - Clear completed rows
 * - Provide view-related data for rendering
 * - Manage overall game state (new game, scoring)
 *
 * Implementations of this interface (e.g., {@link SimpleBoard})
 * contain the actual game logic and matrix manipulation.
 */
public interface Board {

    /**
     * Attempts to move the current brick down by one row.
     *
     * @return true if the brick was successfully moved; false if movement
     *         is blocked (e.g., by the bottom or another brick)
     */
    boolean moveBrickDown();

    /**
     * Attempts to move the current brick one column to the left.
     *
     * @return true if movement succeeds; false if blocked
     */
    boolean moveBrickLeft();

    /**
     * Attempts to move the current brick one column to the right.
     *
     * @return true if movement succeeds; false if blocked
     */
    boolean moveBrickRight();

    /**
     * Attempts to rotate the current brick counter-clockwise.
     *
     * @return true if rotation is valid; false if the rotated shape collides
     *         with existing blocks or leaves the board area
     */
    boolean rotateLeftBrick();

    /**
     * Creates a new active brick at the starting position.
     *
     * @return true if a collision occurs immediately (indicating game over)
     */
    boolean createNewBrick();

    /**
     * Returns the current state of the game board matrix,
     * including all placed bricks.
     *
     * @return a 2D integer matrix representing the board
     */
    int[][] getBoardMatrix();

    /**
     * Provides the data required by the GUI to render the current state:
     * the active brick and its position, as well as the next brick preview.
     *
     * @return a ViewData object containing rendering information
     */
    ViewData getViewData();

    /**
     * Merges the active brick into the background matrix
     * once it can no longer move.
     */
    void mergeBrickToBackground();

    /**
     * Checks the board for any completed rows, clears them,
     * and returns the results (rows cleared, updated matrix, score bonus).
     *
     * @return a RowClearResult describing the outcome of the row-clearing operation
     */
    RowClearResult clearRows();

    /**
     * Returns the score manager for the game.
     *
     * @return the Score object used for tracking player points
     */
    Score getScore();

    /**
     * Resets the board to its initial empty state and starts a new game.
     */
    void newGame();
}

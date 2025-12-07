package com.comp2042;

import java.util.Collections;
import java.util.List;

/**
 * Stores the result of clearing one or more rows in the Tetris game.
 * Uses official Tetris Guideline base scoring:
 * - Single (1 line): 100 points
 * - Double (2 lines): 300 points
 * - Triple (3 lines): 500 points
 * - Tetris (4 lines): 800 points
 * 
 * This includes:
 * - how many rows were cleared (1-4),
 * - the updated game matrix after the clear,
 * - the base points awarded (100/300/500/800),
 * - and the indices of the cleared rows.
 *
 * This class is immutable to ensure the result cannot be modified after creation.
 */
public final class RowClearResult {

    /** Number of rows cleared during this operation. */
    private final int rowsCleared;

    /** The updated game matrix after clearing rows. */
    private final int[][] updatedMatrix;

    /** Points earned for clearing the rows. */
    private final int pointsEarned;
    
    /** List of row indices that were cleared (0-based, from top to bottom). */
    private final List<Integer> clearedRowIndices;

    /**
     * Creates a new RowClearResult.
     *
     * @param rowsCleared     number of rows removed
     * @param updatedMatrix   the updated game matrix after removal
     * @param pointsEarned    the score gained from clearing the rows
     * @param clearedRowIndices the indices of the cleared rows (0-based from top)
     */
    public RowClearResult(int rowsCleared, int[][] updatedMatrix, int pointsEarned, List<Integer> clearedRowIndices) {
        this.rowsCleared = rowsCleared;
        this.updatedMatrix = updatedMatrix;
        this.pointsEarned = pointsEarned;
        this.clearedRowIndices = clearedRowIndices != null ? Collections.unmodifiableList(clearedRowIndices) : Collections.emptyList();
    }

    /**
     * @return number of rows that were cleared
     */
    public int getRowsCleared() {
        return rowsCleared;
    }

    /**
     * @return a deep copy of the updated game matrix after clearing rows
     */
    public int[][] getUpdatedMatrix() {
        return MatrixOperations.copy(updatedMatrix);
    }

    /**
     * @return the number of points earned from clearing rows
     */
    public int getPointsEarned() {
        return pointsEarned;
    }
    
    /**
     * @return an unmodifiable list of row indices that were cleared (0-based from top)
     */
    public List<Integer> getClearedRowIndices() {
        return clearedRowIndices;
    }
    
    /**
     * @return the first cleared row index, or -1 if no rows were cleared
     */
    public int getFirstClearedRowIndex() {
        return clearedRowIndices.isEmpty() ? -1 : clearedRowIndices.get(0);
    }
}

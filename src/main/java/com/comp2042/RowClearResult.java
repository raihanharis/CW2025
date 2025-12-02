package com.comp2042;

/**
 * Stores the result of clearing one or more rows in the Tetris game.
 * This includes:
 * - how many rows were cleared,
 * - the updated game matrix after the clear,
 * - and the points earned from clearing the rows.
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

    /**
     * Creates a new RowClearResult.
     *
     * @param rowsCleared     number of rows removed
     * @param updatedMatrix   the updated game matrix after removal
     * @param pointsEarned    the score gained from clearing the rows
     */
    public RowClearResult(int rowsCleared, int[][] updatedMatrix, int pointsEarned) {
        this.rowsCleared = rowsCleared;
        this.updatedMatrix = updatedMatrix;
        this.pointsEarned = pointsEarned;
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
}

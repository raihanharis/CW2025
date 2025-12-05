package com.comp2042;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class that provides various matrix operations used in the Tetris game,
 * such as copying matrices, merging bricks, checking collisions, and clearing full rows.
 *
 * This class cannot be instantiated.
 */
public final class MatrixOperations {

    /** Prevent instantiation of utility class. */
    private MatrixOperations() { }

    /**
     * Checks whether placing the given brick at the specified (x, y) position
     * would intersect with existing filled cells or the game boundaries.
     *
     * @param matrix the current state of the game board
     * @param brick the brick matrix to test for collision
     * @param x the target X position on the board
     * @param y the target Y position on the board
     * @return true if a collision occurs, false otherwise
     */
    public static boolean intersect(final int[][] matrix, final int[][] brick, int x, int y) {
        for (int row = 0; row < brick.length; row++) {
            for (int col = 0; col < brick[row].length; col++) {

                // Corrected bug: brick[row][col] (not brick[col][row])
                if (brick[row][col] == 0) continue;

                int targetX = x + col;
                int targetY = y + row;

                if (isOutOfBounds(matrix, targetX, targetY) || matrix[targetY][targetX] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the given coordinates lie outside the board.
     * Note: Negative y (above the board) is allowed for spawning bricks.
     */
    private static boolean isOutOfBounds(int[][] matrix, int x, int y) {
        // Allow negative y (brick spawning above board)
        if (y < 0) return false;
        return x < 0 || y >= matrix.length || x >= matrix[y].length;
    }

    /**
     * Creates and returns a deep copy of a matrix.
     *
     * @param original the matrix to copy
     * @return a new deep-copied matrix
     */
    public static int[][] copy(int[][] original) {
        int[][] copied = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copied[i] = new int[original[i].length];
            System.arraycopy(original[i], 0, copied[i], 0, original[i].length);
        }
        return copied;
    }

    /**
     * Merges a brick into the game matrix at a specific location.
     * Does not modify the original matrix; instead returns a modified copy.
     *
     * @param matrix the current game matrix
     * @param brick the brick to merge
     * @param x the X position to merge at
     * @param y the Y position to merge at
     * @return a new matrix containing the merged brick
     */
    public static int[][] merge(int[][] matrix, int[][] brick, int x, int y) {
        int[][] merged = copy(matrix);

        for (int row = 0; row < brick.length; row++) {
            for (int col = 0; col < brick[row].length; col++) {

                if (brick[row][col] == 0) continue;

                int targetX = x + col;
                int targetY = y + row;

                // Skip cells above the board
                if (targetY < 0) continue;

                merged[targetY][targetX] = brick[row][col];
            }
        }
        return merged;
    }

    /**
     * Checks the matrix for full rows, removes them, shifts everything downward,
     * and calculates bonus points.
     *
     * @param matrix the current game matrix
     * @return a RowClearResult object containing:
     *         - number of cleared rows
     *         - updated matrix after removal
     *         - points earned
     */
    public static RowClearResult checkRemoving(final int[][] matrix) {
        int height = matrix.length;
        int width = matrix[0].length;

        int[][] updatedMatrix = new int[height][width];

        Deque<int[]> keptRows = new ArrayDeque<>();
        List<Integer> clearedRowIndices = new ArrayList<>();

        // Scan rows from top to bottom
        for (int r = 0; r < height; r++) {
            int[] rowCopy = new int[width];
            boolean isFull = true;

            for (int c = 0; c < width; c++) {
                rowCopy[c] = matrix[r][c];
                if (matrix[r][c] == 0) {
                    isFull = false;
                }
            }

            if (isFull) {
                clearedRowIndices.add(r);
            } else {
                keptRows.add(rowCopy);
            }
        }

        // Fill updatedMatrix bottom-up with rows kept
        for (int r = height - 1; r >= 0; r--) {
            int[] row = keptRows.pollLast();
            if (row != null) {
                updatedMatrix[r] = row;
            } else {
                break;
            }
        }

        // Scoring formula: N² * 50 encourages clearing multiple rows at once
        int rowsCleared = clearedRowIndices.size();
        int pointsEarned = 50 * rowsCleared * rowsCleared;

        return new RowClearResult(rowsCleared, updatedMatrix, pointsEarned);
    }

    /**
     * Deep copies a list of matrices.
     *
     * @param list the list to deep copy
     * @return a new list containing deep-copied matrices
     */
    public static List<int[][]> deepCopyList(List<int[][]> list) {
        return list.stream()
                .map(MatrixOperations::copy)
                .collect(Collectors.toList());
    }
}

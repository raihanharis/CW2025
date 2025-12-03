package com.comp2042;

/**
 * ViewData contains everything the GUI needs to render the game:
 * - The active falling brick
 * - Its X/Y position on the board
 * - The next brick preview
 * - The CURRENT board matrix (for redrawing background)
 */
public final class ViewData {

    private final int[][] brickData;
    private final int xPosition;
    private final int yPosition;
    private final int[][] nextBrickData;
    private final int[][] boardMatrix;

    public ViewData(int[][] brickData,
                    int xPosition,
                    int yPosition,
                    int[][] nextBrickData,
                    int[][] boardMatrix) {

        this.brickData = brickData;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.nextBrickData = nextBrickData;
        this.boardMatrix = boardMatrix;
    }

    /** Active falling brick shape */
    public int[][] getBrickData() {
        return MatrixOperations.copy(brickData);
    }

    /** Falling brick X position */
    public int getxPosition() {
        return xPosition;
    }

    /** Falling brick Y position */
    public int getyPosition() {
        return yPosition;
    }

    /** Next brick preview shape */
    public int[][] getNextBrickData() {
        return MatrixOperations.copy(nextBrickData);
    }

    /** Full board background (for GUI redraw) */
    public int[][] getBoardMatrix() {
        return MatrixOperations.copy(boardMatrix);
    }
}

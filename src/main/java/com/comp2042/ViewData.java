package com.comp2042;

public final class ViewData {

    private final int[][] brickData;
    private final int xPosition;
    private final int yPosition;
    private final int[][] nextBrickData;
    private final int[][] nextBrick2Data;
    private final int[][] boardMatrix;
    
    // Ghost piece data
    private final int ghostYPosition;

    public ViewData(int[][] brickData, int xPosition, int yPosition, int[][] nextBrickData, int[][] nextBrick2Data, int[][] boardMatrix, int ghostYPosition) {
        this.brickData = brickData;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.nextBrickData = nextBrickData;
        this.nextBrick2Data = nextBrick2Data;
        this.boardMatrix = boardMatrix;
        this.ghostYPosition = ghostYPosition;
    }

    public int[][] getBrickData() {
        return MatrixOperations.copy(brickData);
    }

    public int getxPosition() {
        return xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public int[][] getNextBrickData() {
        return MatrixOperations.copy(nextBrickData);
    }
    
    public int[][] getNextBrick2Data() {
        return MatrixOperations.copy(nextBrick2Data);
    }

    public int[][] getBoardMatrix() {
        return MatrixOperations.copy(boardMatrix);
    }
    
    /**
     * Returns the Y position where the ghost piece should be rendered.
     * The ghost piece shows where the brick will land on hard drop.
     */
    public int getGhostYPosition() {
        return ghostYPosition;
    }
}

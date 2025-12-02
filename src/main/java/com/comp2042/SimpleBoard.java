package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;

import java.awt.*;

/**
 * Represents the active game board, including the current falling brick,
 * the board's occupied cells, scoring, and movement logic.
 *
 * This class coordinates:
 * - brick movement (left, right, down, rotation)
 * - updating the background matrix
 * - clearing rows
 * - tracking score
 * - creating new bricks
 */
public class SimpleBoard implements Board {

    private final int width;
    private final int height;

    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;

    private int[][] currentGameMatrix;
    private Point currentOffset;

    private final Score score;

    /**
     * Creates a new board with the given dimensions.
     *
     * @param width  width of the game matrix
     * @param height height of the game matrix
     */
    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;

        this.currentGameMatrix = new int[width][height];

        this.brickGenerator = new RandomBrickGenerator();
        this.brickRotator = new BrickRotator();
        this.score = new Score();
    }

    /**
     * Attempts to move the current brick one row downward.
     *
     * @return true if the movement succeeds, false if blocked
     */
    @Override
    public boolean moveBrickDown() {
        Point newOffset = new Point(currentOffset);
        newOffset.translate(0, 1);

        boolean collision = MatrixOperations.intersect(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                newOffset.x,
                newOffset.y
        );

        if (collision) return false;

        currentOffset = newOffset;
        return true;
    }

    /**
     * Attempts to move the current brick one tile to the left.
     *
     * @return true if movement succeeds, false otherwise
     */
    @Override
    public boolean moveBrickLeft() {
        Point newOffset = new Point(currentOffset);
        newOffset.translate(-1, 0);

        boolean collision = MatrixOperations.intersect(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                newOffset.x,
                newOffset.y
        );

        if (collision) return false;

        currentOffset = newOffset;
        return true;
    }

    /**
     * Attempts to move the current brick one tile to the right.
     *
     * @return true if movement succeeds, false otherwise
     */
    @Override
    public boolean moveBrickRight() {
        Point newOffset = new Point(currentOffset);
        newOffset.translate(1, 0);

        boolean collision = MatrixOperations.intersect(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                newOffset.x,
                newOffset.y
        );

        if (collision) return false;

        currentOffset = newOffset;
        return true;
    }

    /**
     * Attempts to rotate the current brick clockwise (using BrickRotator).
     *
     * @return true if rotation succeeds, false if blocked
     */
    @Override
    public boolean rotateLeftBrick() {
        int[][] nextShape = brickRotator.getNextShape().getShape();

        boolean collision = MatrixOperations.intersect(
                currentGameMatrix,
                nextShape,
                currentOffset.x,
                currentOffset.y
        );

        if (collision) return false;

        brickRotator.setCurrentShape(brickRotator.getNextShape().getPosition());
        return true;
    }

    /**
     * Generates a new brick and positions it at the spawn point.
     *
     * @return true if the new brick spawns in a blocked position (game over), false otherwise
     */
    @Override
    public boolean createNewBrick() {
        Brick newBrick = brickGenerator.getBrick();
        brickRotator.setBrick(newBrick);

        // Typical Tetris spawn location
        currentOffset = new Point(4, 10);

        return MatrixOperations.intersect(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                currentOffset.x,
                currentOffset.y
        );
    }

    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    /**
     * Returns a snapshot of board/view info used for rendering.
     */
    @Override
    public ViewData getViewData() {
        return new ViewData(
                brickRotator.getCurrentShape(),
                currentOffset.x,
                currentOffset.y,
                brickGenerator.getNextBrick().getShapeMatrix().get(0)
        );
    }

    /**
     * Merges the current falling brick into the background matrix.
     */
    @Override
    public void mergeBrickToBackground() {
        currentGameMatrix = MatrixOperations.merge(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                currentOffset.x,
                currentOffset.y
        );
    }

    /**
     * Clears any full rows using MatrixOperations and updates the score.
     *
     * @return a RowClearResult detailing rows cleared and points earned
     */
    @Override
    public RowClearResult clearRows() {
        RowClearResult result = MatrixOperations.checkRemoving(currentGameMatrix);
        currentGameMatrix = result.getUpdatedMatrix();
        return result;
    }

    @Override
    public Score getScore() {
        return score;
    }

    /**
     * Resets the board to a new game state.
     */
    @Override
    public void newGame() {
        currentGameMatrix = new int[width][height];
        score.reset();
        createNewBrick();
    }
}

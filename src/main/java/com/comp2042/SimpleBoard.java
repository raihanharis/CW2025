package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;

import java.awt.*;
import java.util.Deque;

public class SimpleBoard implements Board {

    private final int width;
    private final int height;
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private int[][] currentGameMatrix;
    private Point currentOffset;
    private final Score score;

    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;

        currentGameMatrix = new int[height][width];
        brickGenerator = new RandomBrickGenerator();
        brickRotator = new BrickRotator();
        score = new Score();
    }

    @Override
    public boolean moveBrickDown() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(0, 1);
        boolean conflict = MatrixOperations.intersect(
                currentMatrix,
                brickRotator.getCurrentShape(),
                (int) p.getX(),
                (int) p.getY()
        );
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean moveBrickLeft() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(-1, 0);

        boolean conflict = MatrixOperations.intersect(
                currentMatrix,
                brickRotator.getCurrentShape(),
                (int) p.getX(),
                (int) p.getY()
        );
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean moveBrickRight() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(1, 0);

        boolean conflict = MatrixOperations.intersect(
                currentMatrix,
                brickRotator.getCurrentShape(),
                (int) p.getX(),
                (int) p.getY()
        );
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean rotateLeftBrick() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        NextShapeInfo nextShape = brickRotator.getNextShape();

        boolean conflict = MatrixOperations.intersect(
                currentMatrix,
                nextShape.getShape(),
                currentOffset.x,
                currentOffset.y
        );

        if (conflict) {
            return false;
        } else {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }
    }

    @Override
    public boolean createNewBrick() {
        Brick currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);

        // Official Tetris: spawn centered at top
        // For 10-column board with 4-wide brick matrix, column 3 centers the piece
        // Spawn at row 0 (top of the hidden spawn area)
        currentOffset = new Point(3, 0);

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

    @Override
    public ViewData getViewData() {
        int ghostY = calculateGhostPosition();
        return new ViewData(
                brickRotator.getCurrentShape(),
                currentOffset.x,
                currentOffset.y,
                brickGenerator.getNextBrick().getShapeMatrix().get(0),
                brickGenerator.getNextBrick2().getShapeMatrix().get(0),
                currentGameMatrix,
                ghostY
        );
    }
    
    /**
     * Calculates the Y position where the ghost piece should be displayed.
     * Simulates dropping the brick straight down until it collides.
     */
    private int calculateGhostPosition() {
        int[][] shape = brickRotator.getCurrentShape();
        int ghostY = currentOffset.y;
        
        // Move down until collision
        while (!MatrixOperations.intersect(currentGameMatrix, shape, currentOffset.x, ghostY + 1)) {
            ghostY++;
        }
        
        return ghostY;
    }

    @Override
    public void mergeBrickToBackground() {
        currentGameMatrix = MatrixOperations.merge(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                currentOffset.x,
                currentOffset.y
        );
    }

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

    @Override
    public void newGame() {
        currentGameMatrix = new int[height][width];
        score.reset();
        createNewBrick();
    }
    
    /**
     * Saves the current game state to a GameState object.
     * This includes board, active brick, next bricks, score, and generator queue.
     */
    public GameState saveState(int totalLinesCleared, int currentLevel, 
                               boolean ghostPieceEnabled, boolean hardDropEnabled, String difficulty) {
        // Get current brick type
        Brick currentBrick = brickRotator.brick; // Access via reflection or add getter
        String activeBrickType = getBrickTypeName(currentBrick);
        int rotationIndex = brickRotator.currentRotationIndex; // Access via reflection or add getter
        
        // Get next brick types
        Brick next1 = brickGenerator.getNextBrick();
        Brick next2 = brickGenerator.getNextBrick2();
        String next1Type = getBrickTypeName(next1);
        String next2Type = getBrickTypeName(next2);
        
        // Get brick generator queue
        Deque<String> queue = ((RandomBrickGenerator) brickGenerator).getQueueAsStrings();
        
        return new GameState(
            currentGameMatrix,
            brickRotator.getCurrentShape(),
            currentOffset.x,
            currentOffset.y,
            rotationIndex,
            activeBrickType,
            next1Type,
            next2Type,
            queue,
            score.scoreProperty().get(),
            totalLinesCleared,
            currentLevel,
            ghostPieceEnabled,
            hardDropEnabled,
            difficulty
        );
    }
    
    /**
     * Restores the game state from a GameState object.
     */
    public void restoreState(GameState state) {
        // Restore board matrix
        currentGameMatrix = state.getBoardMatrix();
        
        // Restore score - reset and add the saved score
        score.reset();
        score.add(state.getScore());
        
        // Restore brick generator queue
        ((RandomBrickGenerator) brickGenerator).restoreQueue(state.getBrickGeneratorQueue());
        
        // Restore active brick
        Brick activeBrick = createBrickFromType(state.getActiveBrickType());
        brickRotator.setBrick(activeBrick);
        brickRotator.setCurrentShape(state.getActiveBrickRotationIndex());
        
        // Restore position
        currentOffset = new Point(state.getActiveBrickX(), state.getActiveBrickY());
    }
    
    /**
     * Helper method to get brick type name from a Brick instance.
     */
    private String getBrickTypeName(Brick brick) {
        if (brick == null) return "IBrick";
        String className = brick.getClass().getSimpleName();
        return className;
    }
    
    /**
     * Helper method to create a Brick instance from a type name.
     * Uses the brick generator's factory method.
     */
    private Brick createBrickFromType(String typeName) {
        return ((RandomBrickGenerator) brickGenerator).createBrickFromType(typeName);
    }
}

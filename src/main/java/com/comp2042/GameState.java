package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import java.awt.Point;
import java.util.Deque;
import java.util.ArrayDeque;

/**
 * Represents the complete state of a Tetris game that can be saved and restored.
 * This includes the board, active piece, next pieces, score, level, and all game settings.
 */
public class GameState {
    
    // Board state
    private final int[][] boardMatrix;
    
    // Active brick state
    private final int[][] activeBrickShape;
    private final int activeBrickX;
    private final int activeBrickY;
    private final int activeBrickRotationIndex;
    private final String activeBrickType; // Store brick type name for restoration
    
    // Next bricks state
    private final String nextBrick1Type;
    private final String nextBrick2Type;
    private final Deque<String> brickGeneratorQueue; // Queue of brick types
    
    // Score and level state
    private final int score;
    private final int totalLinesCleared;
    private final int currentLevel;
    
    // Game settings
    private final boolean ghostPieceEnabled;
    private final boolean hardDropEnabled;
    private final String difficulty; // EASY, MEDIUM, HARD
    
    /**
     * Creates a new GameState from the current game state.
     */
    public GameState(int[][] boardMatrix,
                     int[][] activeBrickShape,
                     int activeBrickX,
                     int activeBrickY,
                     int activeBrickRotationIndex,
                     String activeBrickType,
                     String nextBrick1Type,
                     String nextBrick2Type,
                     Deque<String> brickGeneratorQueue,
                     int score,
                     int totalLinesCleared,
                     int currentLevel,
                     boolean ghostPieceEnabled,
                     boolean hardDropEnabled,
                     String difficulty) {
        // Deep copy board matrix
        this.boardMatrix = MatrixOperations.copy(boardMatrix);
        // Deep copy active brick shape
        this.activeBrickShape = MatrixOperations.copy(activeBrickShape);
        this.activeBrickX = activeBrickX;
        this.activeBrickY = activeBrickY;
        this.activeBrickRotationIndex = activeBrickRotationIndex;
        this.activeBrickType = activeBrickType;
        this.nextBrick1Type = nextBrick1Type;
        this.nextBrick2Type = nextBrick2Type;
        // Deep copy queue
        this.brickGeneratorQueue = new ArrayDeque<>(brickGeneratorQueue);
        this.score = score;
        this.totalLinesCleared = totalLinesCleared;
        this.currentLevel = currentLevel;
        this.ghostPieceEnabled = ghostPieceEnabled;
        this.hardDropEnabled = hardDropEnabled;
        this.difficulty = difficulty;
    }
    
    // Getters
    public int[][] getBoardMatrix() {
        return MatrixOperations.copy(boardMatrix);
    }
    
    public int[][] getActiveBrickShape() {
        return MatrixOperations.copy(activeBrickShape);
    }
    
    public int getActiveBrickX() {
        return activeBrickX;
    }
    
    public int getActiveBrickY() {
        return activeBrickY;
    }
    
    public int getActiveBrickRotationIndex() {
        return activeBrickRotationIndex;
    }
    
    public String getActiveBrickType() {
        return activeBrickType;
    }
    
    public String getNextBrick1Type() {
        return nextBrick1Type;
    }
    
    public String getNextBrick2Type() {
        return nextBrick2Type;
    }
    
    public Deque<String> getBrickGeneratorQueue() {
        return new ArrayDeque<>(brickGeneratorQueue);
    }
    
    public int getScore() {
        return score;
    }
    
    public int getTotalLinesCleared() {
        return totalLinesCleared;
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public boolean isGhostPieceEnabled() {
        return ghostPieceEnabled;
    }
    
    public boolean isHardDropEnabled() {
        return hardDropEnabled;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
}







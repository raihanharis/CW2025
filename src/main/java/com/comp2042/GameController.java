package com.comp2042;

/**
 * Main game controller that coordinates between the game logic (Board) and the UI (GuiController).
 * 
 * <p>This class implements the {@link InputEventListener} interface to handle all user input events
 * from the GUI, such as movement, rotation, and hard drop. It manages the game state, including
 * saving and resuming games, tracking high scores, and coordinating game flow.</p>
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Processes input events and delegates to the Board for game logic</li>
 *   <li>Manages game state persistence for resume functionality</li>
 *   <li>Tracks and updates high score in real-time</li>
 *   <li>Coordinates between Board and GuiController for rendering</li>
 *   <li>Handles game over conditions</li>
 * </ul>
 * </p>
 * 
 * @author Tetris Game Team
 * @version 1.0
 */
public class GameController implements InputEventListener {

    private final Board board;
    private final GuiController gui;
    
    // High score persists during app session (static field)
    private static int highScore = 0;
    
    // Saved game state for resume functionality
    private static GameState savedGameState = null;
    
    // Global game state flag: true when game is in progress, false after Game Over
    private static boolean gameInProgress = false;

    /**
     * Creates a new GameController, either starting a fresh game or resuming from a saved state.
     * 
     * <p>If a saved game state exists, the game will be restored exactly as it was when paused.
     * Otherwise, a new game will be initialized with an empty board and a new brick.</p>
     * 
     * @param gui the GuiController instance that manages the game UI
     */
    public GameController(GuiController gui) {
        this.gui = gui;
        // Official Tetris: 10 columns × 22 rows (20 visible + 2 hidden spawn rows)
        this.board = new SimpleBoard(10, 22);

        gui.setEventListener(this);
        
        // Check if we should resume from saved state
        if (savedGameState != null) {
            resumeFromSavedState();
        } else {
            // Start fresh game
            board.createNewBrick();
            gui.initGameView(board.getBoardMatrix(), board.getViewData());
        }
        
        // Mark game as in progress (either new game or resumed)
        gameInProgress = true;
        
        gui.bindScore(board.getScore().scoreProperty());
        
        // Initialize high score display
        gui.updateHighScore(highScore);
    }
    
    /**
     * Resumes the game from a previously saved state.
     * 
     * <p>This method restores the board matrix, active brick, score, level, lines cleared,
     * and all game settings (difficulty, ghost piece, hard drop) from the saved state.</p>
     */
    private void resumeFromSavedState() {
        SimpleBoard simpleBoard = (SimpleBoard) board;
        
        // Restore board state
        simpleBoard.restoreState(savedGameState);
        
        // Restore UI state (level, lines, score) - must be done BEFORE initGameView
        gui.restoreGameState(savedGameState);
        
        // Restore difficulty speed - must be done BEFORE initGameView so timeline uses correct speed
        SettingsManager settingsManager = SettingsManager.getInstance();
        settingsManager.setDifficulty(SettingsManager.Difficulty.valueOf(savedGameState.getDifficulty()));
        
        // Initialize game view with restored state
        gui.initGameView(board.getBoardMatrix(), board.getViewData());
        
        // Update game speed after view is initialized (timeline is created in initGameView)
        gui.updateDifficultySpeed();
        
        System.out.println("Game resumed from saved state!");
    }
    
    /**
     * Saves the current game state for later resumption.
     * 
     * <p>This method captures the complete game state including the board matrix, active brick,
     * next bricks, score, level, lines cleared, and all settings. The saved state can be
     * restored later when the player chooses to resume the game.</p>
     * 
     * <p>Called automatically when the player returns to the main menu during an active game.</p>
     */
    public void saveGameState() {
        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            
            // Get current UI state
            int totalLinesCleared = gui.getTotalLinesCleared();
            int currentLevel = gui.getCurrentLevel();
            
            // Get settings
            SettingsManager settingsManager = SettingsManager.getInstance();
            boolean ghostPieceEnabled = settingsManager.isGhostPieceEnabled();
            boolean hardDropEnabled = settingsManager.isHardDropEnabled();
            String difficulty = settingsManager.getDifficulty().name();
            
            // Save state
            savedGameState = simpleBoard.saveState(
                totalLinesCleared,
                currentLevel,
                ghostPieceEnabled,
                hardDropEnabled,
                difficulty
            );
            
            // Explicitly mark game as in progress when saving state
            // This ensures the resume button appears even after clearing lines
            gameInProgress = true;
            
            System.out.println("Game state saved! gameInProgress=" + gameInProgress + ", hasSavedState=" + (savedGameState != null));
        }
    }
    
    /**
     * Clears the saved game state.
     * 
     * <p>This method removes any saved game state and marks the game as no longer in progress.
     * Called when starting a new game or restarting the current game.</p>
     */
    public static void clearSavedState() {
        savedGameState = null;
        gameInProgress = false; // Game is no longer in progress after clearing state
    }
    
    /**
     * Checks if there is a saved game state available for resumption.
     * 
     * @return true if a saved game state exists, false otherwise
     */
    public static boolean hasSavedState() {
        return savedGameState != null;
    }
    
    /**
     * Checks if a game is currently in progress (not Game Over).
     * 
     * <p>This flag is used to determine if the "Resume Game" button should be visible
     * in the main menu. The button only appears when a game is actively paused, not
     * after a game over.</p>
     * 
     * @return true if a game is in progress, false if the game has ended
     */
    public static boolean isGameInProgress() {
        return gameInProgress;
    }
    
    /**
     * Sets the game in progress flag.
     * 
     * @param inProgress true to mark game as in progress, false to mark as ended
     */
    public static void setGameInProgress(boolean inProgress) {
        gameInProgress = inProgress;
    }
    
    /**
     * Checks if the current score exceeds the high score and updates it in real-time.
     * 
     * <p>This method is called after every score change (line clears, soft drops, hard drops)
     * to ensure the high score updates immediately during gameplay, not just after game over.</p>
     */
    private void checkAndUpdateHighScore() {
        int currentScore = board.getScore().scoreProperty().get();
        if (currentScore > highScore) {
            highScore = currentScore;
            gui.updateHighScore(highScore);
        }
    }

    /**
     * Handles the down movement event (soft drop).
     * 
     * <p>Moves the active brick down one row. If the brick cannot move down, it is merged
     * into the background and any completed rows are cleared. Awards 1 point per cell
     * for soft drop movement.</p>
     * 
     * @param event the move event containing information about the movement source
     * @return DownData containing the row clear result (if any) and updated view data
     */
    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMove = board.moveBrickDown();
        RowClearResult rowClearResult = null;

        if (!canMove) {
            board.mergeBrickToBackground();
            rowClearResult = board.clearRows();

            if (rowClearResult.getRowsCleared() > 0) {
                // Add official Tetris Guideline base score (100/300/500/800)
                board.getScore().add(rowClearResult.getPointsEarned());
                gui.updateLinesCleared(rowClearResult.getRowsCleared());
                // Show floating score popup at the cleared row location
                int clearedRowIndex = rowClearResult.getFirstClearedRowIndex();
                gui.showScorePopup(rowClearResult.getRowsCleared(), clearedRowIndex);
                // Check and update high score in real-time after line clear
                checkAndUpdateHighScore();
            }

            if (board.createNewBrick()) {
                // Final check when game ends (safety net, but should already be updated)
                checkAndUpdateHighScore();
                gui.gameOver();
            }
        } else {
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
                // Check and update high score in real-time after soft drop
                checkAndUpdateHighScore();
            }
        }

        return new DownData(rowClearResult, board.getViewData());
    }

    /**
     * Handles the left movement event.
     * 
     * @param event the move event containing information about the movement source
     * @return ViewData containing the updated game state for rendering
     */
    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    /**
     * Handles the right movement event.
     * 
     * @param event the move event containing information about the movement source
     * @return ViewData containing the updated game state for rendering
     */
    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    /**
     * Handles the rotation event (counter-clockwise).
     * 
     * @param event the move event containing information about the movement source
     * @return ViewData containing the updated game state for rendering
     */
    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

    /**
     * Handles the hard drop event (instant drop to bottom).
     * 
     * <p>Instantly drops the active brick to the lowest possible valid position.
     * Awards 2 points per cell dropped. After dropping, the brick is merged and
     * any completed rows are cleared.</p>
     * 
     * @return DownData containing the row clear result (if any) and updated view data
     */
    @Override
    public DownData onHardDrop() {
        int dropDistance = 0;

        while (board.moveBrickDown()) {
            dropDistance++;
        }

        board.getScore().add(dropDistance * 2);
        // Check and update high score in real-time after hard drop points
        checkAndUpdateHighScore();
        
        board.mergeBrickToBackground();

        RowClearResult result = board.clearRows();
        if (result.getRowsCleared() > 0) {
            // Add official Tetris Guideline base score (100/300/500/800)
            board.getScore().add(result.getPointsEarned());
            gui.updateLinesCleared(result.getRowsCleared());
            // Show floating score popup at the cleared row location
            int clearedRowIndex = result.getFirstClearedRowIndex();
            gui.showScorePopup(result.getRowsCleared(), clearedRowIndex);
            // Check and update high score in real-time after line clear from hard drop
            checkAndUpdateHighScore();
        }

        if (board.createNewBrick()) {
            // Final check when game ends (safety net, but should already be updated)
            checkAndUpdateHighScore();
            gui.gameOver();
        }

        return new DownData(result, board.getViewData());
    }

    /**
     * Creates a new game by resetting the board and clearing saved state.
     * 
     * <p>This method is called when the player clicks "Restart" or starts a new game.
     * It clears any saved game state and resets the board to its initial empty state.</p>
     */
    @Override
    public void createNewGame() {
        // Clear saved state when restarting
        clearSavedState();
        board.newGame();
        gui.refreshView(board.getViewData());
        // Mark game as in progress after restart
        gameInProgress = true;
    }
}

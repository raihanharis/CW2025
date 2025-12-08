package com.comp2042;

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
     * Creates a new GameController, either starting fresh or resuming from saved state.
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
     * Resumes the game from a saved state.
     */
    private void resumeFromSavedState() {
        SimpleBoard simpleBoard = (SimpleBoard) board;
        
        // Restore board state
        simpleBoard.restoreState(savedGameState);
        
        // Restore UI state (level, lines, score) - must be done BEFORE initGameView
        gui.restoreGameState(savedGameState);
        
        // Restore difficulty speed - must be done BEFORE initGameView so timeline uses correct speed
        AudioManager audioManager = AudioManager.getInstance();
        audioManager.setDifficulty(AudioManager.Difficulty.valueOf(savedGameState.getDifficulty()));
        
        // Initialize game view with restored state
        gui.initGameView(board.getBoardMatrix(), board.getViewData());
        
        // Update game speed after view is initialized (timeline is created in initGameView)
        gui.updateDifficultySpeed();
        
        System.out.println("Game resumed from saved state!");
    }
    
    /**
     * Saves the current game state for later resumption.
     * Called when returning to main menu.
     */
    public void saveGameState() {
        if (board instanceof SimpleBoard) {
            SimpleBoard simpleBoard = (SimpleBoard) board;
            
            // Get current UI state
            int totalLinesCleared = gui.getTotalLinesCleared();
            int currentLevel = gui.getCurrentLevel();
            
            // Get settings
            AudioManager audioManager = AudioManager.getInstance();
            boolean ghostPieceEnabled = audioManager.isGhostPieceEnabled();
            boolean hardDropEnabled = audioManager.isHardDropEnabled();
            String difficulty = audioManager.getDifficulty().name();
            
            // Save state
            savedGameState = simpleBoard.saveState(
                totalLinesCleared,
                currentLevel,
                ghostPieceEnabled,
                hardDropEnabled,
                difficulty
            );
            
            System.out.println("Game state saved!");
        }
    }
    
    /**
     * Clears the saved game state.
     * Called when starting a new game or restarting.
     */
    public static void clearSavedState() {
        savedGameState = null;
        gameInProgress = false; // Game is no longer in progress after clearing state
    }
    
    /**
     * Checks if there is a saved game state.
     */
    public static boolean hasSavedState() {
        return savedGameState != null;
    }
    
    /**
     * Checks if a game is currently in progress (not Game Over).
     * Used to determine if Resume button should be visible.
     */
    public static boolean isGameInProgress() {
        return gameInProgress;
    }
    
    /**
     * Sets the game in progress flag.
     * Called when starting a new game or resuming.
     */
    public static void setGameInProgress(boolean inProgress) {
        gameInProgress = inProgress;
    }
    
    /**
     * Checks if the current score exceeds the high score and updates it in real-time.
     * This method is called after every score change to ensure the high score
     * updates immediately during gameplay, not just after game over.
     */
    private void checkAndUpdateHighScore() {
        int currentScore = board.getScore().scoreProperty().get();
        if (currentScore > highScore) {
            highScore = currentScore;
            gui.updateHighScore(highScore);
        }
    }

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

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

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

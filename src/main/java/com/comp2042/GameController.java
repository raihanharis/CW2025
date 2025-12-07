package com.comp2042;

public class GameController implements InputEventListener {

    private final Board board;
    private final GuiController gui;
    
    // High score persists during app session (static field)
    private static int highScore = 0;

    public GameController(GuiController gui) {
        this.gui = gui;
        // Official Tetris: 10 columns × 22 rows (20 visible + 2 hidden spawn rows)
        this.board = new SimpleBoard(10, 22);

        board.createNewBrick();

        gui.setEventListener(this);
        gui.initGameView(board.getBoardMatrix(), board.getViewData());
        gui.bindScore(board.getScore().scoreProperty());
        
        // Initialize high score display
        gui.updateHighScore(highScore);
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
        board.newGame();
        gui.refreshView(board.getViewData());
    }
}

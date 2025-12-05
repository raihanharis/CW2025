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

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMove = board.moveBrickDown();
        RowClearResult rowClearResult = null;

        if (!canMove) {
            board.mergeBrickToBackground();
            rowClearResult = board.clearRows();

            if (rowClearResult.getRowsCleared() > 0) {
                board.getScore().add(rowClearResult.getPointsEarned());
                gui.updateLinesCleared(rowClearResult.getRowsCleared());
            }

            if (board.createNewBrick()) {
                // Check and update high score when game ends
                int currentScore = board.getScore().scoreProperty().get();
                if (currentScore > highScore) {
                    highScore = currentScore;
                    gui.updateHighScore(highScore);
                }
                gui.gameOver();
            }
        } else {
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
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
        board.mergeBrickToBackground();

        RowClearResult result = board.clearRows();
        if (result.getRowsCleared() > 0) {
            board.getScore().add(result.getPointsEarned());
            gui.updateLinesCleared(result.getRowsCleared());
        }

        if (board.createNewBrick()) {
            // Check and update high score when game ends
            int currentScore = board.getScore().scoreProperty().get();
            if (currentScore > highScore) {
                highScore = currentScore;
                gui.updateHighScore(highScore);
            }
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

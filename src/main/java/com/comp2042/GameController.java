package com.comp2042;

/**
 * The central controller for game logic and communication between
 * the model (Board) and the view (GuiController).
 *
 * Responsibilities:
 * - Respond to user input events (left, right, rotate, down, hard drop).
 * - Update the board state and score accordingly.
 * - Notify the GUI when the game view must update.
 * - Detect and handle game-over conditions.
 */
public class GameController implements InputEventListener {

    /** The main game board containing brick states, score, and logic. */
    private final Board board = new SimpleBoard(25, 10);

    /** Handles updates to the graphical user interface. */
    private final GuiController guiController;

    /**
     * Creates a GameController and links it to a GUI controller.
     *
     * @param guiController the GUI controller to communicate with
     */
    public GameController(GuiController guiController) {
        this.guiController = guiController;

        board.createNewBrick(); // spawn first brick

        guiController.setEventListener(this);
        guiController.initGameView(board.getBoardMatrix(), board.getViewData());
        guiController.bindScore(board.getScore().scoreProperty());
    }

    /**
     * Handles the "down" event. If the brick cannot move further down,
     * merges it into the background, clears rows, updates score, and
     * spawns a new brick.
     */
    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMoveDown = board.moveBrickDown();
        RowClearResult rowClearResult = null;

        if (!canMoveDown) {
            board.mergeBrickToBackground();
            rowClearResult = board.clearRows();

            // Add score for cleared rows
            if (rowClearResult.getRowsCleared() > 0) {
                board.getScore().add(rowClearResult.getPointsEarned());
            }

            // New brick, check game over
            if (board.createNewBrick()) {
                guiController.gameOver();
            }

            guiController.refreshGameBackground(board.getBoardMatrix());

        } else {
            // Soft drop (user presses DOWN)
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
            }
        }

        return new DownData(rowClearResult, board.getViewData());
    }

    /**
     * HARD DROP — instantly drop the current brick to its lowest position.
     * Called when SPACE is pressed.
     */
    @Override
    public DownData onHardDrop() {
        int dropDistance = 0;
        RowClearResult rowClearResult = null;

        // Move brick down repeatedly until blocked
        while (board.moveBrickDown()) {
            dropDistance++;
        }

        // Add hard drop score bonus (2 points per tile dropped)
        board.getScore().add(dropDistance * 2);

        // Merge to background
        board.mergeBrickToBackground();

        // Clear rows
        rowClearResult = board.clearRows();
        if (rowClearResult.getRowsCleared() > 0) {
            board.getScore().add(rowClearResult.getPointsEarned());
        }

        // Spawn new brick
        if (board.createNewBrick()) {
            guiController.gameOver();
        }

        guiController.refreshGameBackground(board.getBoardMatrix());

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
    public void createNewGame() {
        board.newGame();
        guiController.refreshGameBackground(board.getBoardMatrix());
    }
}

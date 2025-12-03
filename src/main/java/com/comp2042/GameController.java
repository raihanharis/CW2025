package com.comp2042;

/**
 * The central controller for game logic and communication between
 * the model (Board) and the view (GuiController).
 *
 * Responsibilities:
 * - Respond to user input events (left, right, rotate, down).
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
     *
     * @param event move event information
     * @return updated DownData showing the result of the action
     */
    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMoveDown = board.moveBrickDown();
        RowClearResult rowClearResult = null;

        if (!canMoveDown) {

            // Merge brick into background and clear rows if needed
            board.mergeBrickToBackground();
            rowClearResult = board.clearRows();

            // Award points for cleared rows
            if (rowClearResult.getRowsCleared() > 0) {
                board.getScore().add(rowClearResult.getPointsEarned());
            }

            // Spawn new brick — if blocked, game is over
            if (board.createNewBrick()) {
                guiController.gameOver();
            }

            guiController.refreshGameBackground(board.getBoardMatrix());

        } else {
            // User manually accelerated the brick downward
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
            }
        }

        return new DownData(rowClearResult, board.getViewData());
    }

    /**
     * Handles left movement by the player.
     */
    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    /**
     * Handles right movement by the player.
     */
    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    /**
     * Handles rotation input.
     */
    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

    /**
     * Resets the game to a fresh state.
     */
    @Override
    public void createNewGame() {
        board.newGame();
        guiController.refreshGameBackground(board.getBoardMatrix());
    }
}

package com.comp2042;

/**
 * The central controller for game logic and communication between
 * the model (Board) and the view (GuiController).
 *
 * Responsibilities:
 * - Respond to user input events (left, right, rotate, down, hard drop)
 * - Update board state and scoring
 * - Notify the GUI with updated ViewData
 * - Detect and handle game-over
 */
public class GameController implements InputEventListener {

    private final Board board = new SimpleBoard(25, 10);
    private final GuiController guiController;

    public GameController(GuiController guiController) {
        this.guiController = guiController;

        board.createNewBrick(); // first brick

        guiController.setEventListener(this);
        guiController.initGameView(board.getBoardMatrix(), buildViewData());
        guiController.bindScore(board.getScore().scoreProperty());
    }

    /**
     * Creates a ViewData package containing everything the GUI needs.
     */
    private ViewData buildViewData() {
        return new ViewData(
                board.getViewData().getBrickData(),                 // active brick shape
                board.getViewData().getxPosition(),                 // x pos
                board.getViewData().getyPosition(),                 // y pos
                board.getViewData().getNextBrickData(),             // next preview
                board.getBoardMatrix()                              // board background (NEW)
        );
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMoveDown = board.moveBrickDown();
        RowClearResult rowClearResult = null;

        if (!canMoveDown) {

            board.mergeBrickToBackground();
            rowClearResult = board.clearRows();

            // award row-clear points
            if (rowClearResult.getRowsCleared() > 0) {
                board.getScore().add(rowClearResult.getPointsEarned());
            }

            // new brick required
            if (board.createNewBrick()) {
                guiController.gameOver();
            }

            // update background
            guiController.refreshGameBackground(board.getBoardMatrix());
        } else {
            // soft drop score for user input
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
            }
        }

        return new DownData(rowClearResult, buildViewData());
    }

    /**
     * HARD DROP: instantly drop as far as possible.
     */
    @Override
    public DownData onHardDrop() {
        int dropDistance = 0;

        // move down repeatedly
        while (board.moveBrickDown()) {
            dropDistance++;
        }

        // bonus score: 2 per tile dropped
        board.getScore().add(dropDistance * 2);

        // merge and clear
        board.mergeBrickToBackground();
        RowClearResult result = board.clearRows();

        if (result.getRowsCleared() > 0) {
            board.getScore().add(result.getPointsEarned());
        }

        // attempt new brick
        if (board.createNewBrick()) {
            guiController.gameOver();
        }

        guiController.refreshGameBackground(board.getBoardMatrix());

        return new DownData(result, buildViewData());
    }

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return buildViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return buildViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return buildViewData();
    }

    @Override
    public void createNewGame() {
        board.newGame();
        guiController.refreshGameBackground(board.getBoardMatrix());
    }
}

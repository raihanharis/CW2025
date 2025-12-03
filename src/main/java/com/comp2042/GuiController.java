package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * GUI Controller for rendering and handling UI events.
 */
public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20; // size of rectangles

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane brickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    // board background tiles
    private Rectangle[][] boardTiles;

    // active falling brick tiles
    private Rectangle[][] activeBrickTiles;

    // next brick preview tiles
    private Rectangle[][] nextBrickTiles;

    private InputEventListener eventListener;
    private Timeline timeline;

    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FontLoader.loadDigitalFont();

        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();

        gamePanel.setOnKeyPressed(this::handleKeyPress);

        gameOverPanel.setVisible(false);

        Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);
    }

    // ------------------------------------------------------------
    // KEY HANDLING
    // ------------------------------------------------------------

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();

        if (code == KeyCode.R) {
            newGame(null);
            return;
        }

        if (code == KeyCode.P) {
            togglePause();
            return;
        }

        if (code == KeyCode.SPACE && !isPause.get() && !isGameOver.get()) {
            handleHardDrop();
            return;
        }

        if (isPause.get() || isGameOver.get()) return;

        switch (code) {
            case LEFT, A -> refreshView(eventListener.onLeftEvent(
                    new MoveEvent(EventType.LEFT, EventSource.USER)));

            case RIGHT, D -> refreshView(eventListener.onRightEvent(
                    new MoveEvent(EventType.RIGHT, EventSource.USER)));

            case UP, W -> refreshView(eventListener.onRotateEvent(
                    new MoveEvent(EventType.ROTATE, EventSource.USER)));

            case DOWN, S -> handleDown(new MoveEvent(EventType.DOWN, EventSource.USER));
        }

        event.consume();
    }

    // ------------------------------------------------------------
    // INITIAL SETUP
    // ------------------------------------------------------------

    public void initGameView(int[][] boardMatrix, ViewData viewData) {

        // -------------------------------
        // CREATE BACKGROUND MATRIX
        // -------------------------------
        boardTiles = new Rectangle[boardMatrix.length][boardMatrix[0].length];

        for (int r = 0; r < boardMatrix.length; r++) {
            for (int c = 0; c < boardMatrix[r].length; c++) {
                Rectangle t = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                t.setFill(Color.TRANSPARENT);
                boardTiles[r][c] = t;
                gamePanel.add(t, c, r);
            }
        }

        // -------------------------------
        // CREATE ACTIVE BRICK MATRIX
        // -------------------------------
        int[][] shape = viewData.getBrickData();
        activeBrickTiles = new Rectangle[shape.length][shape[0].length];

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                Rectangle t = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                t.setFill(Color.TRANSPARENT);
                activeBrickTiles[r][c] = t;
                gamePanel.add(t, c, r); // will be repositioned in refresh
            }
        }

        // -------------------------------
        // NEXT BRICK PREVIEW
        // -------------------------------
        int[][] next = viewData.getNextBrickData();
        nextBrickTiles = new Rectangle[next.length][next[0].length];

        for (int r = 0; r < next.length; r++) {
            for (int c = 0; c < next[r].length; c++) {
                Rectangle t = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                t.setFill(Color.TRANSPARENT);
                nextBrickTiles[r][c] = t;
                brickPanel.add(t, c, r);
            }
        }

        refreshView(viewData);

        // -------------------------------
        // AUTO FALL
        // -------------------------------
        timeline = new Timeline(new KeyFrame(
                Duration.millis(400),
                e -> handleDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // ------------------------------------------------------------
    // DRAWING HELPERS
    // ------------------------------------------------------------

    private void refreshView(ViewData viewData) {
        drawBoard(viewData.getBoardMatrix());
        drawActiveBrick(viewData.getBrickData(), viewData.getxPosition(), viewData.getyPosition());
        drawNextBrick(viewData.getNextBrickData());
    }

    private void drawBoard(int[][] matrix) {
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                boardTiles[r][c].setFill(getFill(matrix[r][c]));
            }
        }
    }

    private void drawActiveBrick(int[][] data, int x, int y) {
        for (int r = 0; r < data.length; r++) {
            for (int c = 0; c < data[r].length; c++) {
                Rectangle tile = activeBrickTiles[r][c];

                if (data[r][c] == 0) {
                    tile.setFill(Color.TRANSPARENT);
                    continue;
                }

                tile.setFill(getFill(data[r][c]));
                gamePanel.setRowIndex(tile, y + r);
                gamePanel.setColumnIndex(tile, x + c);
            }
        }
    }

    private void drawNextBrick(int[][] next) {
        for (int r = 0; r < next.length; r++) {
            for (int c = 0; c < next[r].length; c++) {
                nextBrickTiles[r][c].setFill(getFill(next[r][c]));
            }
        }
    }

    private Paint getFill(int v) {
        return switch (v) {
            case 0 -> Color.TRANSPARENT;
            case 1 -> Color.AQUA;
            case 2 -> Color.BLUEVIOLET;
            case 3 -> Color.DARKGREEN;
            case 4 -> Color.YELLOW;
            case 5 -> Color.RED;
            case 6 -> Color.BEIGE;
            case 7 -> Color.BURLYWOOD;
            default -> Color.WHITE;
        };
    }

    // ------------------------------------------------------------
    // GAME ACTIONS
    // ------------------------------------------------------------

    private void handleDown(MoveEvent event) {
        DownData result = eventListener.onDownEvent(event);
        refreshView(result.getViewData());
    }

    private void handleHardDrop() {
        DownData res = eventListener.onHardDrop();
        refreshView(res.getViewData());

        if (res.getClearRow() != null && res.getClearRow().getRowsCleared() > 0) {
            NotificationPanel notif = new NotificationPanel("+" + res.getClearRow().getPointsEarned());
            groupNotification.getChildren().add(notif);
            notif.showScore(groupNotification.getChildren());
        }
    }

    // ------------------------------------------------------------
    // GAME STATE
    // ------------------------------------------------------------

    private void togglePause() {
        if (isPause.get()) {
            isPause.set(false);
            if (timeline != null) timeline.play();
        } else {
            isPause.set(true);
            if (timeline != null) timeline.stop();
        }
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty property) { }

    public void gameOver() {
        isGameOver.set(true);
        timeline.stop();
        gameOverPanel.setVisible(true);
    }

    public void newGame(ActionEvent e) {
        isGameOver.set(false);
        isPause.set(false);
        gameOverPanel.setVisible(false);
        eventListener.createNewGame();
    }
}

/** Loads digital font safely */
class FontLoader {
    public static void loadDigitalFont() {
        try {
            Font.loadFont(
                    FontLoader.class.getClassLoader()
                            .getResource("digital.ttf").toExternalForm(),
                    38
            );
        } catch (Exception ignored) {}
    }
}

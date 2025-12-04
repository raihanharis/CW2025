package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
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

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane brickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    private Rectangle[][] boardTiles;
    private Rectangle[][] activeBrickTiles;
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
    }

    private void handleKeyPress(KeyEvent event) {
        if (isPause.get() || isGameOver.get()) return;

        KeyCode code = event.getCode();

        switch (code) {
            case LEFT, A -> refreshView(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
            case RIGHT, D -> refreshView(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
            case UP, W -> refreshView(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
            case DOWN, S -> handleDown(new MoveEvent(EventType.DOWN, EventSource.USER));
            case SPACE -> handleHardDrop();
            case P -> togglePause();
            case R -> newGame(null);
        }
    }

    public void initGameView(int[][] boardMatrix, ViewData viewData) {
        boardTiles = new Rectangle[boardMatrix.length][boardMatrix[0].length];

        for (int r = 0; r < boardMatrix.length; r++) {
            for (int c = 0; c < boardMatrix[r].length; c++) {
                Rectangle rect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rect.setFill(Color.TRANSPARENT);
                boardTiles[r][c] = rect;
                gamePanel.add(rect, c, r);
            }
        }

        int[][] shape = viewData.getBrickData();
        activeBrickTiles = new Rectangle[shape.length][shape[0].length];

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                Rectangle rect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rect.setFill(Color.TRANSPARENT);
                activeBrickTiles[r][c] = rect;
                gamePanel.add(rect, c, r);
            }
        }

        int[][] next = viewData.getNextBrickData();
        nextBrickTiles = new Rectangle[next.length][next[0].length];

        for (int r = 0; r < next.length; r++) {
            for (int c = 0; c < next[r].length; c++) {
                Rectangle rect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rect.setFill(Color.TRANSPARENT);
                nextBrickTiles[r][c] = rect;
                brickPanel.add(rect, c, r);
            }
        }

        refreshView(viewData);

        timeline = new Timeline(new KeyFrame(
                Duration.millis(400),
                e -> handleDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void refreshView(ViewData viewData) {
        drawBoard(viewData);
        drawActiveBrick(viewData);
        drawNextBrick(viewData);
    }

    private void drawBoard(ViewData viewData) {
        int[][] board = viewData.getBoardMatrix();

        for (int r = 0; r < board.length && r < boardTiles.length; r++) {
            for (int c = 0; c < board[r].length && c < boardTiles[r].length; c++) {
                boardTiles[r][c].setFill(getFill(board[r][c]));
            }
        }
    }

    private void drawActiveBrick(ViewData data) {
        int[][] mat = data.getBrickData();
        int x = data.getxPosition();
        int y = data.getyPosition();

        for (int r = 0; r < mat.length; r++) {
            for (int c = 0; c < mat[r].length; c++) {
                Rectangle tile = activeBrickTiles[r][c];

                if (mat[r][c] == 0) {
                    tile.setFill(Color.TRANSPARENT);
                    continue;
                }

                tile.setFill(getFill(mat[r][c]));
                gamePanel.setRowIndex(tile, y + r);
                gamePanel.setColumnIndex(tile, x + c);
            }
        }
    }

    private void drawNextBrick(ViewData data) {
        int[][] next = data.getNextBrickData();
        for (int r = 0; r < next.length; r++) {
            for (int c = 0; c < next[r].length; c++) {
                nextBrickTiles[r][c].setFill(getFill(next[r][c]));
            }
        }
    }

    private Paint getFill(int v) {
        return switch (v) {
            case 1 -> Color.AQUA;
            case 2 -> Color.BLUEVIOLET;
            case 3 -> Color.DARKGREEN;
            case 4 -> Color.YELLOW;
            case 5 -> Color.RED;
            case 6 -> Color.BEIGE;
            case 7 -> Color.BURLYWOOD;
            default -> Color.TRANSPARENT;
        };
    }

    private void handleDown(MoveEvent event) {
        DownData result = eventListener.onDownEvent(event);
        refreshView(result.getViewData());
    }

    private void handleHardDrop() {
        DownData result = eventListener.onHardDrop();
        refreshView(result.getViewData());
    }

    private void togglePause() {
        if (isPause.get()) {
            isPause.set(false);
            timeline.play();
        } else {
            isPause.set(true);
            timeline.stop();
        }
    }

    public void setEventListener(InputEventListener listener) {
        this.eventListener = listener;
    }

    public void bindScore(IntegerProperty property) {}

    public void gameOver() {
        isGameOver.set(true);
        timeline.stop();
        gameOverPanel.setVisible(true);
    }

    public void newGame(ActionEvent event) {
        isGameOver.set(false);
        isPause.set(false);
        gameOverPanel.setVisible(false);
        eventListener.createNewGame();
    }

    static class FontLoader {
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
}
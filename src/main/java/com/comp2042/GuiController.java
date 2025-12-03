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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.text.Font;

/**
 * Handles all UI-related tasks for the Tetris game.
 * Includes input, rendering, notifications, pause, restart, and hard drop.
 */
public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;

    @FXML private GridPane gamePanel;
    @FXML private Group groupNotification;
    @FXML private GridPane brickPanel;
    @FXML private GameOverPanel gameOverPanel;

    private Rectangle[][] displayMatrix;
    private Rectangle[][] rectangles;
    private InputEventListener eventListener;
    private Timeline timeLine;

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

    /**
     * Handles keyboard input including movement, rotation, pause, restart, and hard drop.
     */
    private void handleKeyPress(KeyEvent keyEvent) {
        KeyCode code = keyEvent.getCode();

        // Restart game (R)
        if (code == KeyCode.R) {
            newGame(null);
            keyEvent.consume();
            return;
        }

        // Pause / Unpause (P)
        if (code == KeyCode.P) {
            togglePause();
            keyEvent.consume();
            return;
        }

        // Hard Drop (SPACE)
        if (code == KeyCode.SPACE && !isPause.get() && !isGameOver.get()) {
            performHardDrop();
            keyEvent.consume();
            return;
        }

        // Prevent movement while paused or game over
        if (isPause.get() || isGameOver.get()) return;

        // Movement / rotation
        switch (code) {
            case LEFT, A -> refreshBrick(eventListener.onLeftEvent(
                    new MoveEvent(EventType.LEFT, EventSource.USER)));

            case RIGHT, D -> refreshBrick(eventListener.onRightEvent(
                    new MoveEvent(EventType.RIGHT, EventSource.USER)));

            case UP, W -> refreshBrick(eventListener.onRotateEvent(
                    new MoveEvent(EventType.ROTATE, EventSource.USER)));

            case DOWN, S -> moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
        }

        keyEvent.consume();
    }

    /**
     * Performs the hard drop (SPACE key).
     */
    private void performHardDrop() {
        DownData result = eventListener.onHardDrop();

        if (result.getClearRow() != null && result.getClearRow().getRowsCleared() > 0) {
            NotificationPanel notification =
                    new NotificationPanel("+" + result.getClearRow().getPointsEarned());
            groupNotification.getChildren().add(notification);
            notification.showScore(groupNotification.getChildren());
        }

        refreshBrick(result.getViewData());
        gamePanel.requestFocus();
    }

    /**
     * Toggles pause state.
     */
    private void togglePause() {
        if (isPause.get()) {
            isPause.set(false);
            if (timeLine != null) timeLine.play();
        } else {
            isPause.set(true);
            if (timeLine != null) timeLine.stop();
        }
        gamePanel.requestFocus();
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {

        // Background tiles
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];

        for (int row = 2; row < boardMatrix.length; row++) {
            for (int col = 0; col < boardMatrix[row].length; col++) {
                Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                r.setFill(Color.TRANSPARENT);

                displayMatrix[row][col] = r;
                gamePanel.add(r, col, row - 2);
            }
        }

        // Active brick
        int[][] shape = brick.getBrickData();
        rectangles = new Rectangle[shape.length][shape[0].length];

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                r.setFill(getFillColor(shape[row][col]));

                rectangles[row][col] = r;
                brickPanel.add(r, col, row);
            }
        }

        updateBrickPosition(brick);

        // Auto-fall
        timeLine = new Timeline(new KeyFrame(
                Duration.millis(400),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }

    private Paint getFillColor(int value) {
        return switch (value) {
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

    private void updateBrickPosition(ViewData brick) {
        brickPanel.setLayoutX(
                gamePanel.getLayoutX() +
                        brick.getxPosition() * (brickPanel.getVgap() + BRICK_SIZE));

        brickPanel.setLayoutY(
                gamePanel.getLayoutY() - 42 +
                        brick.getyPosition() * (brickPanel.getHgap() + BRICK_SIZE));
    }

    private void refreshBrick(ViewData brick) {
        if (!isPause.get()) {
            updateBrickPosition(brick);

            int[][] data = brick.getBrickData();
            for (int row = 0; row < data.length; row++) {
                for (int col = 0; col < data[row].length; col++) {
                    setRectangleData(data[row][col], rectangles[row][col]);
                }
            }
        }
    }

    public void refreshGameBackground(int[][] board) {
        for (int row = 2; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                setRectangleData(board[row][col], displayMatrix[row][col]);
            }
        }
    }

    private void setRectangleData(int color, Rectangle rect) {
        rect.setFill(getFillColor(color));
        rect.setArcHeight(9);
        rect.setArcWidth(9);
    }

    private void moveDown(MoveEvent event) {
        if (!isPause.get()) {
            DownData result = eventListener.onDownEvent(event);

            if (result.getClearRow() != null && result.getClearRow().getRowsCleared() > 0) {
                NotificationPanel notification =
                        new NotificationPanel("+" + result.getClearRow().getPointsEarned());
                groupNotification.getChildren().add(notification);
                notification.showScore(groupNotification.getChildren());
            }

            refreshBrick(result.getViewData());
        }

        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty property) { }

    public void gameOver() {
        timeLine.stop();
        gameOverPanel.setVisible(true);
        isGameOver.set(true);
    }

    public void newGame(ActionEvent event) {
        timeLine.stop();
        gameOverPanel.setVisible(false);

        eventListener.createNewGame();

        refreshGameBackground(eventListener.onDownEvent(
                new MoveEvent(EventType.DOWN, EventSource.THREAD)
        ).getViewData().getBrickData());

        gamePanel.requestFocus();
        timeLine.play();

        isPause.set(false);
        isGameOver.set(false);
    }

    public void pauseGame(ActionEvent event) {
        togglePause();
    }
}

/** Utility to load custom font. */
class FontLoader {
    public static void loadDigitalFont() {
        try {
            Font.loadFont(
                    FontLoader.class.getClassLoader()
                            .getResource("digital.ttf").toExternalForm(), 38
            );
        } catch (Exception ignored) {}
    }
}

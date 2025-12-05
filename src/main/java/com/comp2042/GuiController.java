package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    // Board dimensions (official Tetris)
    private static final int BOARD_COLS = 10;
    private static final int VISIBLE_ROWS = 20;
    private static final int HIDDEN_ROWS = 2;
    private static final int TOTAL_ROWS = VISIBLE_ROWS + HIDDEN_ROWS;
    
    // Tile sizing
    private static final int MIN_TILE_SIZE = 15;
    private static final int MAX_TILE_SIZE = 35;
    private static final int DEFAULT_TILE_SIZE = 22;
    
    private int currentTileSize = DEFAULT_TILE_SIZE;

    @FXML private StackPane rootPane;
    @FXML private HBox gameContainer;
    @FXML private GridPane gamePanel;
    @FXML private VBox nextPanel;
    @FXML private Label nextLabel;
    @FXML private GridPane brickPanel;
    @FXML private Group groupNotification;
    @FXML private GameOverPanel gameOverPanel;

    private Rectangle[][] boardTiles;
    private Rectangle[][] activeBrickTiles;
    private Rectangle[][] nextBrickTiles;

    private InputEventListener eventListener;
    private Timeline timeline;

    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);
    
    private ViewData lastViewData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FontLoader.loadDigitalFont();
        
        // Set up key handling on root pane
        rootPane.setFocusTraversable(true);
        rootPane.setOnKeyPressed(this::handleKeyPress);
        
        gameOverPanel.setVisible(false);
        
        // Add resize listener for scaling
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> handleResize());
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> handleResize());
    }
    
    private void handleResize() {
        if (rootPane.getWidth() <= 0 || rootPane.getHeight() <= 0) return;
        
        // Calculate optimal tile size based on available space
        double availableWidth = rootPane.getWidth() - 120; // Reserve space for NEXT panel
        double availableHeight = rootPane.getHeight() - 40; // Small margin
        
        // Calculate tile size to fit the board
        int tileByWidth = (int) (availableWidth / (BOARD_COLS + 1)); // +1 for gaps
        int tileByHeight = (int) (availableHeight / (VISIBLE_ROWS + 1));
        
        int newTileSize = Math.min(tileByWidth, tileByHeight);
        newTileSize = Math.max(MIN_TILE_SIZE, Math.min(MAX_TILE_SIZE, newTileSize));
        
        if (newTileSize != currentTileSize && boardTiles != null) {
            currentTileSize = newTileSize;
            resizeTiles();
        }
    }
    
    private void resizeTiles() {
        // Resize board tiles
        if (boardTiles != null) {
            for (int r = 0; r < boardTiles.length; r++) {
                for (int c = 0; c < boardTiles[r].length; c++) {
                    if (boardTiles[r][c] != null) {
                        boardTiles[r][c].setWidth(currentTileSize);
                        boardTiles[r][c].setHeight(currentTileSize);
                    }
                }
            }
        }
        
        // Resize active brick tiles
        if (activeBrickTiles != null) {
            for (int r = 0; r < activeBrickTiles.length; r++) {
                for (int c = 0; c < activeBrickTiles[r].length; c++) {
                    if (activeBrickTiles[r][c] != null) {
                        activeBrickTiles[r][c].setWidth(currentTileSize);
                        activeBrickTiles[r][c].setHeight(currentTileSize);
                    }
                }
            }
        }
        
        // Resize next brick tiles
        if (nextBrickTiles != null) {
            for (int r = 0; r < nextBrickTiles.length; r++) {
                for (int c = 0; c < nextBrickTiles[r].length; c++) {
                    if (nextBrickTiles[r][c] != null) {
                        nextBrickTiles[r][c].setWidth(currentTileSize);
                        nextBrickTiles[r][c].setHeight(currentTileSize);
                    }
                }
            }
        }
        
        // Update label font size
        nextLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: " + (currentTileSize * 0.6) + ";");
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
        lastViewData = viewData;
        
        // Clear existing tiles
        gamePanel.getChildren().clear();
        brickPanel.getChildren().clear();
        
        int cols = boardMatrix[0].length;
        boardTiles = new Rectangle[TOTAL_ROWS][cols];

        // Create board tiles (only visible rows added to grid)
        for (int r = 0; r < TOTAL_ROWS; r++) {
            for (int c = 0; c < cols; c++) {
                Rectangle rect = new Rectangle(currentTileSize, currentTileSize);
                rect.setFill(Color.web("#111111"));
                boardTiles[r][c] = rect;
                if (r >= HIDDEN_ROWS) {
                    gamePanel.add(rect, c, r - HIDDEN_ROWS);
                }
            }
        }

        // Create active brick tiles
        int[][] shape = viewData.getBrickData();
        activeBrickTiles = new Rectangle[shape.length][shape[0].length];

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                Rectangle rect = new Rectangle(currentTileSize, currentTileSize);
                rect.setFill(Color.TRANSPARENT);
                activeBrickTiles[r][c] = rect;
                gamePanel.add(rect, c, r);
            }
        }

        // Create next brick tiles
        int[][] next = viewData.getNextBrickData();
        nextBrickTiles = new Rectangle[next.length][next[0].length];

        for (int r = 0; r < next.length; r++) {
            for (int c = 0; c < next[r].length; c++) {
                Rectangle rect = new Rectangle(currentTileSize, currentTileSize);
                rect.setFill(Color.TRANSPARENT);
                nextBrickTiles[r][c] = rect;
                brickPanel.add(rect, c, r);
            }
        }

        refreshView(viewData);
        
        // Request focus for keyboard input
        rootPane.requestFocus();

        // Start game loop
        timeline = new Timeline(new KeyFrame(
                Duration.millis(400),
                e -> handleDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void refreshView(ViewData viewData) {
        lastViewData = viewData;
        drawBoard(viewData);
        drawActiveBrick(viewData);
        drawNextBrick(viewData);
    }

    private void drawBoard(ViewData viewData) {
        int[][] board = viewData.getBoardMatrix();

        for (int r = HIDDEN_ROWS; r < board.length && r < boardTiles.length; r++) {
            for (int c = 0; c < board[r].length && c < boardTiles[r].length; c++) {
                Paint fill = board[r][c] == 0 ? Color.web("#111111") : getFill(board[r][c]);
                boardTiles[r][c].setFill(fill);
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
                int targetRow = y + r;
                int visibleRow = targetRow - HIDDEN_ROWS;

                if (mat[r][c] == 0 || visibleRow < 0) {
                    tile.setFill(Color.TRANSPARENT);
                    continue;
                }

                tile.setFill(getFill(mat[r][c]));
                GridPane.setRowIndex(tile, visibleRow);
                GridPane.setColumnIndex(tile, x + c);
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
            case 1 -> Color.CYAN;
            case 2 -> Color.BLUEVIOLET;
            case 3 -> Color.LIMEGREEN;
            case 4 -> Color.YELLOW;
            case 5 -> Color.RED;
            case 6 -> Color.ORANGE;
            case 7 -> Color.DEEPSKYBLUE;
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
        rootPane.requestFocus();
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

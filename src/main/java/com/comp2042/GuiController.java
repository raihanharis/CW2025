package com.comp2042;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.Animation;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
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
    @FXML private VBox leftPanel;
    @FXML private GridPane gamePanel;
    @FXML private VBox nextPanel;
    @FXML private Label nextLabel;
    @FXML private GridPane brickPanel;
    @FXML private Group groupNotification;
    @FXML private VBox pauseOverlay;
    @FXML private Label pauseLabel;
    @FXML private GameOverPanel gameOverPanel;
    
    // Left panel controls
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label linesLabel;
    @FXML private Button restartButton;
    @FXML private Button pauseButton;

    private Rectangle[][] boardTiles;
    private Rectangle[][] activeBrickTiles;
    private Rectangle[][] ghostBrickTiles;  // Ghost piece tiles
    private Rectangle[][] nextBrickTiles;

    private InputEventListener eventListener;
    private Timeline timeline;
    
    // Pause animations
    private FadeTransition pauseFadeIn;
    private FadeTransition pauseFadeOut;
    private Timeline pausePulseAnimation;

    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);
    
    private ViewData lastViewData;
    
    // Game stats
    private int totalLinesCleared = 0;
    private int currentLevel = 1;
    private static final int LINES_PER_LEVEL = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FontLoader.loadDigitalFont();
        
        // Set up key handling on root pane
        rootPane.setFocusTraversable(true);
        rootPane.setOnKeyPressed(this::handleKeyPress);
        
        gameOverPanel.setVisible(false);
        
        // Make pause overlay non-blocking for input
        if (pauseOverlay != null) {
            pauseOverlay.setMouseTransparent(true);
        }
        
        // Initialize pause animations
        initializePauseAnimations();
        
        // Initialize button animations
        initializeButtonAnimations();
        
        // Add resize listener for scaling
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> handleResize());
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> handleResize());
    }
    
    /**
     * Initializes hover and press animations for game buttons.
     */
    private void initializeButtonAnimations() {
        if (restartButton != null) {
            setupButtonAnimations(restartButton, "#00d4ff");
        }
        if (pauseButton != null) {
            setupButtonAnimations(pauseButton, "#00ff88");
        }
    }
    
    /**
     * Sets up hover and press animations for a button.
     * @param button The button to animate
     * @param glowColor The neon glow color (hex format)
     */
    private void setupButtonAnimations(Button button, String glowColor) {
        // Hover scale animation (1.0 -> 1.05)
        ScaleTransition hoverScale = new ScaleTransition(Duration.millis(150), button);
        hoverScale.setToX(1.05);
        hoverScale.setToY(1.05);
        
        // Unhover scale animation (1.05 -> 1.0)
        ScaleTransition unhoverScale = new ScaleTransition(Duration.millis(150), button);
        unhoverScale.setToX(1.0);
        unhoverScale.setToY(1.0);
        
        // Press scale animation (1.05 -> 0.95)
        ScaleTransition pressScale = new ScaleTransition(Duration.millis(100), button);
        pressScale.setToX(0.95);
        pressScale.setToY(0.95);
        
        // Release scale animation (0.95 -> 1.05)
        ScaleTransition releaseScale = new ScaleTransition(Duration.millis(100), button);
        releaseScale.setToX(1.05);
        releaseScale.setToY(1.05);
        
        // Enhanced glow effect for hover
        DropShadow hoverGlow = new DropShadow();
        hoverGlow.setColor(javafx.scene.paint.Color.web(glowColor));
        hoverGlow.setRadius(20);
        hoverGlow.setSpread(0.8);
        
        DropShadow normalGlow = new DropShadow();
        normalGlow.setColor(javafx.scene.paint.Color.web(glowColor));
        normalGlow.setRadius(15);
        normalGlow.setSpread(0.6);
        
        // Hover event handlers
        button.setOnMouseEntered(e -> {
            hoverScale.play();
            button.setEffect(hoverGlow);
        });
        
        button.setOnMouseExited(e -> {
            unhoverScale.play();
            button.setEffect(normalGlow);
        });
        
        // Press event handlers
        button.setOnMousePressed(e -> {
            pressScale.play();
        });
        
        button.setOnMouseReleased(e -> {
            releaseScale.play();
        });
        
        // Initial glow effect
        button.setEffect(normalGlow);
    }
    
    /**
     * Initializes the pause overlay animations: fade in/out and pulsing glow effect.
     */
    private void initializePauseAnimations() {
        if (pauseOverlay == null || pauseLabel == null) return;
        
        // Fade in animation (300ms)
        pauseFadeIn = new FadeTransition(Duration.millis(300), pauseOverlay);
        pauseFadeIn.setFromValue(0.0);
        pauseFadeIn.setToValue(1.0);
        
        // Fade out animation (300ms)
        pauseFadeOut = new FadeTransition(Duration.millis(300), pauseOverlay);
        pauseFadeOut.setFromValue(1.0);
        pauseFadeOut.setToValue(0.0);
        pauseFadeOut.setOnFinished(e -> pauseOverlay.setVisible(false));
        
        // Pulsing glow effect for the pause label
        Glow glow = new Glow();
        pauseLabel.setEffect(glow);
        
        pausePulseAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, e -> glow.setLevel(0.3)),
            new KeyFrame(Duration.millis(1000), e -> glow.setLevel(0.8)),
            new KeyFrame(Duration.millis(2000), e -> glow.setLevel(0.3))
        );
        pausePulseAnimation.setCycleCount(Animation.INDEFINITE);
        
        // Initially invisible
        pauseOverlay.setOpacity(0.0);
        pauseOverlay.setVisible(false);
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
        
        // Resize ghost brick tiles
        if (ghostBrickTiles != null) {
            for (int r = 0; r < ghostBrickTiles.length; r++) {
                for (int c = 0; c < ghostBrickTiles[r].length; c++) {
                    if (ghostBrickTiles[r][c] != null) {
                        ghostBrickTiles[r][c].setWidth(currentTileSize);
                        ghostBrickTiles[r][c].setHeight(currentTileSize);
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
        KeyCode code = event.getCode();
        
        // P key always works (to toggle pause)
        if (code == KeyCode.P && !isGameOver.get()) {
            togglePause();
            return;
        }
        
        // R key works even when paused (to start new game)
        if (code == KeyCode.R) {
            newGame(null);
            return;
        }
        
        // All other keys blocked when paused or game over
        if (isPause.get() || isGameOver.get()) return;

        switch (code) {
            case LEFT, A -> refreshView(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
            case RIGHT, D -> refreshView(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
            case UP, W -> refreshView(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
            case DOWN, S -> handleDown(new MoveEvent(EventType.DOWN, EventSource.USER));
            case SPACE -> handleHardDrop();
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

        // Create ghost brick tiles (added first so they render behind active brick)
        int[][] shape = viewData.getBrickData();
        ghostBrickTiles = new Rectangle[shape.length][shape[0].length];

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                Rectangle rect = new Rectangle(currentTileSize, currentTileSize);
                rect.setFill(Color.TRANSPARENT);
                ghostBrickTiles[r][c] = rect;
                gamePanel.add(rect, c, r);
            }
        }

        // Create active brick tiles (added after ghost so they render on top)
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
        drawGhostBrick(viewData);  // Draw ghost first (behind active)
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

    private void drawGhostBrick(ViewData data) {
        int[][] mat = data.getBrickData();
        int x = data.getxPosition();
        int ghostY = data.getGhostYPosition();
        int activeY = data.getyPosition();

        for (int r = 0; r < mat.length; r++) {
            for (int c = 0; c < mat[r].length; c++) {
                Rectangle tile = ghostBrickTiles[r][c];
                int targetRow = ghostY + r;
                int visibleRow = targetRow - HIDDEN_ROWS;

                // Don't show ghost if it overlaps with active brick or is above visible area
                if (mat[r][c] == 0 || visibleRow < 0 || ghostY == activeY) {
                    tile.setFill(Color.TRANSPARENT);
                    continue;
                }

                tile.setFill(getGhostFill(mat[r][c]));
                GridPane.setRowIndex(tile, visibleRow);
                GridPane.setColumnIndex(tile, x + c);
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
    
    /**
     * Returns a semi-transparent version of the brick color for the ghost piece.
     */
    private Paint getGhostFill(int v) {
        return switch (v) {
            case 1 -> Color.CYAN.deriveColor(0, 1, 1, 0.25);
            case 2 -> Color.BLUEVIOLET.deriveColor(0, 1, 1, 0.25);
            case 3 -> Color.LIMEGREEN.deriveColor(0, 1, 1, 0.25);
            case 4 -> Color.YELLOW.deriveColor(0, 1, 1, 0.25);
            case 5 -> Color.RED.deriveColor(0, 1, 1, 0.25);
            case 6 -> Color.ORANGE.deriveColor(0, 1, 1, 0.25);
            case 7 -> Color.DEEPSKYBLUE.deriveColor(0, 1, 1, 0.25);
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
            // Resume game - fade out animation
            isPause.set(false);
            fadeOutPauseOverlay();
            timeline.play();
        } else {
            // Pause game - fade in animation
            isPause.set(true);
            fadeInPauseOverlay();
            timeline.stop();
        }
        updatePauseButtonText();
    }
    
    /**
     * Fades in the pause overlay with smooth animation.
     */
    private void fadeInPauseOverlay() {
        if (pauseOverlay == null) return;
        
        pauseOverlay.setVisible(true);
        pauseOverlay.setOpacity(0.0);
        
        // Stop any ongoing fade out
        if (pauseFadeOut.getStatus() == Animation.Status.RUNNING) {
            pauseFadeOut.stop();
        }
        
        // Start fade in
        pauseFadeIn.play();
        
        // Start pulsing glow animation
        if (pausePulseAnimation != null) {
            pausePulseAnimation.play();
        }
    }
    
    /**
     * Fades out the pause overlay with smooth animation.
     */
    private void fadeOutPauseOverlay() {
        if (pauseOverlay == null) return;
        
        // Stop pulsing animation
        if (pausePulseAnimation != null) {
            pausePulseAnimation.stop();
        }
        
        // Stop any ongoing fade in
        if (pauseFadeIn.getStatus() == Animation.Status.RUNNING) {
            pauseFadeIn.stop();
        }
        
        // Start fade out
        pauseFadeOut.play();
    }

    public void setEventListener(InputEventListener listener) {
        this.eventListener = listener;
    }

    public void bindScore(IntegerProperty property) {
        if (scoreLabel != null && property != null) {
            scoreLabel.textProperty().bind(property.asString());
        }
    }
    
    /**
     * Updates the lines cleared count and calculates level.
     */
    public void updateLinesCleared(int linesJustCleared) {
        if (linesJustCleared > 0) {
            totalLinesCleared += linesJustCleared;
            currentLevel = 1 + (totalLinesCleared / LINES_PER_LEVEL);
            
            if (linesLabel != null) {
                linesLabel.setText(String.valueOf(totalLinesCleared));
            }
            if (levelLabel != null) {
                levelLabel.setText(String.valueOf(currentLevel));
            }
            
            // Increase game speed with level
            updateGameSpeed();
        }
    }
    
    /**
     * Updates the game speed based on current level.
     */
    private void updateGameSpeed() {
        if (timeline != null) {
            // Base speed 400ms, decrease by 25ms per level, minimum 100ms
            int speed = Math.max(100, 400 - (currentLevel - 1) * 25);
            timeline.stop();
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(speed),
                    e -> handleDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
            ));
            if (!isPause.get() && !isGameOver.get()) {
                timeline.play();
            }
        }
    }
    
    /**
     * Resets game statistics for a new game.
     */
    private void resetStats() {
        totalLinesCleared = 0;
        currentLevel = 1;
        if (linesLabel != null) {
            linesLabel.setText("0");
        }
        if (levelLabel != null) {
            levelLabel.setText("1");
        }
    }

    public void gameOver() {
        isGameOver.set(true);
        timeline.stop();
        gameOverPanel.setVisible(true);
    }

    public void newGame(ActionEvent event) {
        isGameOver.set(false);
        isPause.set(false);
        gameOverPanel.setVisible(false);
        
        // Stop and hide pause overlay immediately
        if (pausePulseAnimation != null) {
            pausePulseAnimation.stop();
        }
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(false);
            pauseOverlay.setOpacity(0.0);
        }
        
        updatePauseButtonText();
        resetStats();
        
        // Reset game speed to default
        if (timeline != null) {
            timeline.stop();
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(400),
                    e -> handleDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
            ));
        }
        
        eventListener.createNewGame();
        
        if (timeline != null && !isPause.get()) {
            timeline.play();
        }
        
        rootPane.requestFocus();
    }
    
    /**
     * Button handler for Restart button click.
     */
    @FXML
    private void onRestartClick(ActionEvent event) {
        newGame(event);
    }
    
    /**
     * Button handler for Pause button click.
     */
    @FXML
    private void onPauseClick(ActionEvent event) {
        if (!isGameOver.get()) {
            togglePause();
        }
        rootPane.requestFocus();
    }
    
    /**
     * Updates the pause button text based on current state.
     */
    private void updatePauseButtonText() {
        if (pauseButton != null) {
            pauseButton.setText(isPause.get() ? "▶ RESUME" : "⏸ PAUSE");
        }
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

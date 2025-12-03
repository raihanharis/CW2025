package com.comp2042;

/**
 * Interface for handling all input events from the GUI.
 * The GameController implements this to process movement,
 * rotation, hard drop, and game reset events.
 */
public interface InputEventListener {

    DownData onDownEvent(MoveEvent event);

    ViewData onLeftEvent(MoveEvent event);

    ViewData onRightEvent(MoveEvent event);

    ViewData onRotateEvent(MoveEvent event);

    /**
     * Instantly drops the active brick to the lowest possible valid position.
     *
     * @return DownData containing updated board state and row clear information
     */
    DownData onHardDrop();   // <-- ADDED FOR SPACE KEY HARD DROP

    void createNewGame();
}

package com.comp2042;

import com.comp2042.logic.bricks.Brick;

/**
 * Handles rotation of the current brick by tracking which shape index
 * (rotation state) is active and providing access to the next rotation.
 *
 * Each Brick contains several rotation states stored as a list of 2D matrices.
 * This class cycles through them when rotating.
 */
public class BrickRotator {

    /** The current brick being rotated. */
    private Brick brick;

    /** The index representing the current rotation state of the brick. */
    private int currentRotationIndex = 0;

    /**
     * Returns the next rotation state of the brick without changing the current one.
     *
     * @return information about the next rotation (shape matrix + index)
     */
    public NextShapeInfo getNextShape() {
        int nextIndex = (currentRotationIndex + 1) % brick.getShapeMatrix().size();
        return new NextShapeInfo(brick.getShapeMatrix().get(nextIndex), nextIndex);
    }

    /**
     * Gets the current rotation matrix of the brick.
     *
     * @return 2D matrix representing the brick's current rotation
     */
    public int[][] getCurrentShape() {
        return brick.getShapeMatrix().get(currentRotationIndex);
    }

    /**
     * Sets the brick's current rotation state.
     *
     * @param rotationIndex the index of the new rotation state
     */
    public void setCurrentShape(int rotationIndex) {
        this.currentRotationIndex = rotationIndex;
    }

    /**
     * Assigns a new brick to the rotator and resets rotation to the default state.
     *
     * @param brick the brick to rotate
     */
    public void setBrick(Brick brick) {
        this.brick = brick;
        this.currentRotationIndex = 0; // Reset rotation when a new brick appears
    }
}

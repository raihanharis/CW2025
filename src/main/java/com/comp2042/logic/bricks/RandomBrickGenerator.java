package com.comp2042.logic.bricks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomBrickGenerator implements BrickGenerator {

    private final List<Brick> brickList;

    private final Deque<Brick> nextBricks = new ArrayDeque<>();

    public RandomBrickGenerator() {
        brickList = new ArrayList<>();
        brickList.add(new IBrick());
        brickList.add(new JBrick());
        brickList.add(new LBrick());
        brickList.add(new OBrick());
        brickList.add(new SBrick());
        brickList.add(new TBrick());
        brickList.add(new ZBrick());
        // Initialize with two next bricks
        nextBricks.add(brickList.get(ThreadLocalRandom.current().nextInt(brickList.size())));
        nextBricks.add(brickList.get(ThreadLocalRandom.current().nextInt(brickList.size())));
    }

    @Override
    public Brick getBrick() {
        // Always maintain at least 2 bricks in queue
        // If we have 2 or fewer, add one before consuming
        if (nextBricks.size() <= 2) {
            nextBricks.add(brickList.get(ThreadLocalRandom.current().nextInt(brickList.size())));
        }
        return nextBricks.poll();
    }

    @Override
    public Brick getNextBrick() {
        return nextBricks.peek();
    }
    
    @Override
    public Brick getNextBrick2() {
        // Get the second brick in queue
        if (nextBricks.size() >= 2) {
            Brick[] bricks = nextBricks.toArray(new Brick[0]);
            return bricks[1];
        }
        // If only one brick, generate a new one for preview
        if (nextBricks.size() == 1) {
            Brick newBrick = brickList.get(ThreadLocalRandom.current().nextInt(brickList.size()));
            nextBricks.add(newBrick);
            return newBrick;
        }
        // Fallback: generate a random brick
        return brickList.get(ThreadLocalRandom.current().nextInt(brickList.size()));
    }
}

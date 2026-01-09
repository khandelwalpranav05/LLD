package com.snake.model;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Snake - Manages the snake's body and movement logic
 * 
 * KEY DESIGN DECISIONS:
 * 1. Deque<Cell> for Body: Allows O(1) addition to head and removal from tail.
 * 2. Set<Cell> for Collision: Allows O(1) check if a cell hits the body.
 */
public class Snake {
    private final Deque<Cell> body;
    private final Set<Cell> bodySet;

    public Snake(Cell initPos) {
        this.body = new LinkedList<>();
        this.bodySet = new HashSet<>();
        
        // Initialize with one cell
        this.body.add(initPos);
        this.bodySet.add(initPos);
    }

    public Cell getHead() {
        return body.peekFirst();
    }

    /**
     * Calculate next head position based on current head and direction
     */
    public Cell getNextHead(Direction direction) {
        Cell head = getHead();
        int row = head.getRow();
        int col = head.getCol();

        switch (direction) {
            case UP:    row--; break;
            case DOWN:  row++; break;
            case LEFT:  col--; break;
            case RIGHT: col++; break;
        }
        return new Cell(row, col);
    }

    /**
     * Move the snake to the new head position.
     * Removes tail to maintain length (Slide effect).
     */
    public void move(Cell newHead) {
        // Add new head
        body.addFirst(newHead);
        bodySet.add(newHead);

        // Remove old tail
        Cell tail = body.removeLast();
        bodySet.remove(tail);
    }

    /**
     * Grow the snake by adding new head but NOT removing tail.
     */
    public void grow(Cell newHead) {
        body.addFirst(newHead);
        bodySet.add(newHead);
    }

    /**
     * Check if the given cell crashes into the snake body.
     * 
     * @param nextHead The cell we are moving into
     * @param isGrowing Whether we are eating food (growing) this turn
     * @return true if crash, false if safe
     */
    public boolean checkCrash(Cell nextHead, boolean isGrowing) {
        // Step 1: Not in body at all = definitely safe
        if (!bodySet.contains(nextHead)) {
            return false;
        }

        // Step 2: It's in the body. Is it the TAIL? (The special case)
        Cell currentTail = body.peekLast();
        if (nextHead.equals(currentTail)) {
            // Tail is special: behavior depends on whether snake is growing
            // - Growing: tail STAYS in place = crash
            // - Not growing: tail VACATES this cell = safe (we're chasing our tail)
            return isGrowing;
        }

        // Step 3: Hitting any OTHER body part (neck, middle) = always crash
        return true;
    }
    
    // Kept for backward compatibility if needed, strict by default
    public boolean checkCrash(Cell cell) {
        return bodySet.contains(cell);
    }
    
    public Deque<Cell> getBody() {
        return body;
    }
}

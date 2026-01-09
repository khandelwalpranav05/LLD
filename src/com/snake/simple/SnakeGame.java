package com.snake.simple;

import java.util.*;

/**
 * SIMPLIFIED Snake Game - Interview-Friendly Version
 * 
 * KEY DATA STRUCTURES:
 * 1. Deque<Cell> body  - For O(1) add head / remove tail
 * 2. Set<Cell> bodySet - For O(1) collision check
 * 
 * This single class contains all core logic for interview brevity.
 */
public class SnakeGame {
    
    // Board
    public final int height, width;
    public Cell food;
    private final Random random = new Random();
    
    // Snake (THE KEY DATA STRUCTURES)
    public Deque<Cell> body = new LinkedList<>();
    public Set<Cell> bodySet = new HashSet<>();
    
    // Game State
    public boolean gameOver = false;
    public int score = 0;
    
    public SnakeGame(int height, int width) {
        this.height = height;
        this.width = width;
        
        // Start snake at center
        Cell start = new Cell(height / 2, width / 2);
        body.add(start);
        bodySet.add(start);
        
        // Generate initial food
        generateFood();
    }
    
    /**
     * Main game logic - process one move
     */
    public void move(Direction dir) {
        if (gameOver) return;
        
        Cell nextHead = body.peekFirst().move(dir);
        
        // 1. Wall collision
        if (isWall(nextHead)) {
            gameOver = true;
            return;
        }
        
        // 2. Check if eating food (needed for self-collision logic)
        boolean eating = nextHead.equals(food);
        
        // 3. Self collision
        if (hitsBody(nextHead, eating)) {
            gameOver = true;
            return;
        }
        
        // 4. Execute move
        if (eating) {
            grow(nextHead);
            score++;
            generateFood();
        } else {
            moveSnake(nextHead);
        }
    }
    
    // ========== SNAKE OPERATIONS ==========
    
    /**
     * Move: add head, remove tail (length same)
     */
    private void moveSnake(Cell newHead) {
        body.addFirst(newHead);
        bodySet.add(newHead);
        
        Cell tail = body.removeLast();
        bodySet.remove(tail);
    }
    
    /**
     * Grow: add head, keep tail (length +1)
     */
    private void grow(Cell newHead) {
        body.addFirst(newHead);
        bodySet.add(newHead);
        // No tail removal!
    }
    
    /**
     * THE TRICKY PART: Self collision detection
     * 
     * Case 1: Not in body at all → SAFE
     * Case 2: In body + eating → CRASH (tail stays)
     * Case 3: In body + not eating + hitting tail → SAFE (tail leaves)
     * Case 4: In body + not eating + hitting other → CRASH
     */
    private boolean hitsBody(Cell nextHead, boolean isEating) {
        // Not in body = definitely safe
        if (!bodySet.contains(nextHead)) {
            return false;
        }
        
        // In body + eating = tail stays = any hit is crash
        if (isEating) {
            return true;
        }
        
        // In body + not eating = tail will leave
        // Safe ONLY if hitting the tail (which will vacate)
        Cell tail = body.peekLast();
        return !nextHead.equals(tail);
    }
    
    // ========== BOARD OPERATIONS ==========
    
    private boolean isWall(Cell c) {
        return c.row < 0 || c.row >= height || 
               c.col < 0 || c.col >= width;
    }
    
    private void generateFood() {
        Cell newFood;
        do {
            newFood = new Cell(
                random.nextInt(height),
                random.nextInt(width)
            );
        } while (bodySet.contains(newFood));
        this.food = newFood;
    }
    
    // ========== GETTERS ==========
    
    public Cell getHead() {
        return body.peekFirst();
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public int getScore() {
        return score;
    }
}


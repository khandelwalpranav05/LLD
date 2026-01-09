package com.snake.simple;

/**
 * Simple enum for movement directions
 */
public enum Direction {
    UP(-1, 0), DOWN(1, 0), LEFT(0, -1), RIGHT(0, 1);
    
    public final int dr, dc;  // delta row, delta col
    
    Direction(int dr, int dc) {
        this.dr = dr;
        this.dc = dc;
    }
}


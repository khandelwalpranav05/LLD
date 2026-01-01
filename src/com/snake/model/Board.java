package com.snake.model;

import java.util.Random;

/**
 * Board - Manages grid dimensions and food generation
 */
public class Board {
    private final int height; // rows
    private final int width;  // cols
    private Cell food;
    private final Random random;

    public Board(int height, int width) {
        this.height = height;
        this.width = width;
        this.random = new Random();
        generateFood(); // Initial food
    }

    // Generate random food
    // Ideally we pass snake to ensure we don't spawn on body
    public void generateFood() {
        int r = random.nextInt(height);
        int c = random.nextInt(width);
        this.food = new Cell(r, c);
    }

    // Overloaded to avoid collision with snake immediately
    public void generateFood(Snake snake) {
        Cell newFood;
        while(true) {
            int r = random.nextInt(height);
            int c = random.nextInt(width);
            newFood = new Cell(r, c);
            
            if (!snake.checkCrash(newFood)) {
                break;
            }
        }
        this.food = newFood;
    }

    public boolean isWall(Cell cell) {
        return cell.getRow() < 0 || cell.getRow() >= height ||
               cell.getCol() < 0 || cell.getCol() >= width;
    }

    public Cell getFood() {
        return food;
    }
    
    public int getHeight() { return height; }
    public int getWidth() { return width; }
}

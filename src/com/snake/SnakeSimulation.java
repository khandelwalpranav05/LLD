package com.snake;

import com.snake.game.SnakeGame;
import com.snake.model.Cell;
import com.snake.model.Direction;

/**
 * SnakeSimulation - Non-interactive runner to verify game logic
 */
public class SnakeSimulation {
    
    public static void main(String[] args) {
        System.out.println("=== SNAKE GAME LOGIC VERIFICATION ===");
        
        // 1. Initialize
        SnakeGame game = new SnakeGame(5, 5); // Small board
        System.out.println("Initialized 5x5 Board.");
        printState(game);
        
        // 2. Move RIGHT twice
        System.out.println("\n--- Move RIGHT ---");
        game.move(Direction.RIGHT);
        printState(game);
        
        System.out.println("\n--- Move RIGHT ---");
        game.move(Direction.RIGHT);
        printState(game);
        
        // 3. Move DOWN
        System.out.println("\n--- Move DOWN ---");
        game.move(Direction.DOWN);
        printState(game);
        
        // 4. Force Collision (Move Left into itself? No, head is at (2,4) or so)
        // Let's verify coordinates
        Cell head = game.getSnake().getHead();
        System.out.println("\nCurrent Head: " + head);
        
        // 5. Try to Hit Wall
        System.out.println("\n--- Move RIGHT until Wall Crash ---");
        while (!game.isGameOver()) {
            game.move(Direction.RIGHT);
            System.out.println("Moved RIGHT. Game Over? " + game.isGameOver());
        }
        System.out.println("Wall Crash Verified!");
    }

    private static void printState(SnakeGame game) {
        int h = game.getBoard().getHeight();
        int w = game.getBoard().getWidth();
        char[][] grid = new char[h][w];

        for(int r=0; r<h; r++) for(int c=0; c<w; c++) grid[r][c] = '.';
        
        // Food
        Cell food = game.getBoard().getFood();
        grid[food.getRow()][food.getCol()] = 'X';
        
        // Snake
        for(Cell c : game.getSnake().getBody()) grid[c.getRow()][c.getCol()] = 'o';
        Cell head = game.getSnake().getHead();
        grid[head.getRow()][head.getCol()] = 'H'; // Head

        for(int r=0; r<h; r++) {
            System.out.print("[ ");
            for(int c=0; c<w; c++) System.out.print(grid[r][c] + " ");
            System.out.println("]");
        }
    }
}

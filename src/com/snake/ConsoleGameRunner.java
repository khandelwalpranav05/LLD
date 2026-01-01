package com.snake;

import com.snake.game.SnakeGame;
import com.snake.model.Cell;
import com.snake.model.Direction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * ConsoleGameRunner - Runs the Snake Game in terminal
 * IMPL: Uses 2 threads (Game Loop + Input Listener)
 */
public class ConsoleGameRunner {
    
    // Volatile is crucial for visibility across threads
    private static volatile Direction currentDirection = Direction.RIGHT;
    private static volatile boolean isRunning = true;

    public static void main(String[] args) throws InterruptedException {
        // 10x10 Board
        SnakeGame game = new SnakeGame(10, 10);
        
        System.out.println("=== SNAKE GAME STARTED ===");
        System.out.println("Controls: W (Up), A (Left), S (Down), D (Right)");
        System.out.println("Press ENTER after typing direction letter.");

        // --- THREAD 1: Input Listener ---
        Thread inputThread = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (isRunning) {
                    String line = reader.readLine();
                    if (line == null) break;
                    line = line.trim().toUpperCase();
                    
                    if (line.isEmpty()) continue;
                    
                    switch (line.charAt(0)) {
                        case 'W': 
                            if (currentDirection != Direction.DOWN) currentDirection = Direction.UP; 
                            break;
                        case 'S': 
                            if (currentDirection != Direction.UP) currentDirection = Direction.DOWN; 
                            break;
                        case 'A': 
                            if (currentDirection != Direction.RIGHT) currentDirection = Direction.LEFT; 
                            break;
                        case 'D': 
                            if (currentDirection != Direction.LEFT) currentDirection = Direction.RIGHT; 
                            break;
                        case 'Q':
                            isRunning = false;
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        inputThread.setDaemon(true); // Allow JVM to exit if this is stuck
        inputThread.start();

        // --- THREAD 2: Game Loop (Main Thread) ---
        while (isRunning && !game.isGameOver()) {
            // 1. Clear Console (Simulation)
            System.out.println("\n\n\n\n\n\n\n\n\n"); 
            System.out.println("Score: " + game.getScore());
            
            // 2. Move Logic
            game.move(currentDirection);
            
            // 3. Render
            printBoard(game);
            
            if (game.isGameOver()) {
                System.out.println("!!! GAME OVER !!!");
                System.out.println("Final Score: " + game.getScore());
                isRunning = false;
                break;
            }

            // 4. Tick Speed (200ms -> 5 FPS)
            Thread.sleep(500); // Slow enough for manual typing
        }
        
        System.out.println("Exiting...");
    }

    private static void printBoard(SnakeGame game) {
        int h = game.getBoard().getHeight();
        int w = game.getBoard().getWidth();
        char[][] grid = new char[h][w];

        // Fill Empty
        for(int r=0; r<h; r++) 
            for(int c=0; c<w; c++) grid[r][c] = '.';

        // Draw Food
        Cell food = game.getBoard().getFood();
        grid[food.getRow()][food.getCol()] = 'X';

        // Draw Snake
        for(Cell c : game.getSnake().getBody()) {
            grid[c.getRow()][c.getCol()] = 'O'; 
        }
        // Head override
        Cell head = game.getSnake().getHead();
        grid[head.getRow()][head.getCol()] = 'H';

        // Print Grid
        for(int r=0; r<h; r++) {
            for(int c=0; c<w; c++) {
                System.out.print(grid[r][c] + " ");
            }
            System.out.println();
        }
    }
}

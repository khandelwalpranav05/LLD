package com.snake.game;

import com.snake.model.Board;
import com.snake.model.Cell;
import com.snake.model.Direction;
import com.snake.model.Snake;

/**
 * SnakeGame - Core Game Logic Engine
 */
public class SnakeGame {
    private final Snake snake;
    private final Board board;
    private boolean isGameOver;
    private int score;

    public SnakeGame(int height, int width) {
        this.board = new Board(height, width);
        // Start snake at center
        this.snake = new Snake(new Cell(height / 2, width / 2));
        this.isGameOver = false;
        this.score = 0;
        
        // Ensure food doesn't spawn on snake initially
        this.board.generateFood(snake);
    }

    /**
     * Move the game state forward by one step in the given direction.
     */
    public void move(Direction direction) {
        if (isGameOver) return;

        Cell nextHead = snake.getNextHead(direction);

        // 1. Check Wall Collision
        if (board.isWall(nextHead)) {
            isGameOver = true;
            return;
        }

        // 2. Check Food (Pre-check to know if we are growing)
        boolean willGrow = nextHead.equals(board.getFood());

        // 3. Check Self Collision
        // Pass 'willGrow' because if we grow, tail stays (collision fatal).
        // If we move, tail leaves (collision with tail safe).
        if (snake.checkCrash(nextHead, willGrow)) {
             isGameOver = true;
             return;
        }

        // 4. Execute Move/Grow
        if (willGrow) {
            // Eat Food: Grow + Score + New Food
            snake.grow(nextHead);
            score++;
            board.generateFood(snake);
        } else {
            // Empty Cell: Just Move
            snake.move(nextHead);
        }
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public int getScore() {
        return score;
    }

    public Board getBoard() {
        return board;
    }

    public Snake getSnake() {
        return snake;
    }
}

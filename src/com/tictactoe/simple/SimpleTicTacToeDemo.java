package com.tictactoe.simple;

/**
 * Demo for Simplified Tic Tac Toe
 */
public class SimpleTicTacToeDemo {
    
    public static void main(String[] args) {
        System.out.println("=== SIMPLE TIC TAC TOE DEMO ===\n");
        
        // 1. Create Players
        Player player1 = new Player("Alice", Symbol.X);
        Player player2 = new Player("Bob", Symbol.O);
        
        // 2. Create Game (3x3 board)
        Game game = new Game(3, player1, player2);
        
        // 3. Simulate a game - X wins diagonally
        System.out.println("--- Simulating a game where X wins ---\n");
        
        game.makeMove(0, 0);  // X at (0,0)
        game.makeMove(0, 1);  // O at (0,1)
        game.makeMove(1, 1);  // X at (1,1) - center
        game.makeMove(0, 2);  // O at (0,2)
        game.makeMove(2, 2);  // X at (2,2) - X wins diagonal!
        
        System.out.println("\n--- Demo 2: Testing Draw ---\n");
        
        // Create new game for draw scenario
        Game game2 = new Game(3, player1, player2);
        
        /*
         * Draw scenario:
         * X | O | X
         * X | O | O
         * O | X | X
         */
        game2.makeMove(0, 0);  // X
        game2.makeMove(0, 1);  // O
        game2.makeMove(0, 2);  // X
        game2.makeMove(1, 1);  // O
        game2.makeMove(1, 0);  // X
        game2.makeMove(2, 0);  // O
        game2.makeMove(1, 2);  // X
        game2.makeMove(2, 2);  // O
        game2.makeMove(2, 1);  // X - Draw!
        
        System.out.println("\n--- Demo 3: Invalid Move ---\n");
        
        Game game3 = new Game(3, player1, player2);
        game3.makeMove(1, 1);  // X at center
        game3.makeMove(1, 1);  // O tries same cell - INVALID!
        
        System.out.println("\n--- Demo 4: 4x4 Board ---\n");
        
        Game game4 = new Game(4, player1, player2);
        game4.makeMove(0, 0);  // X
        game4.makeMove(1, 0);  // O
        game4.makeMove(0, 1);  // X
        game4.makeMove(1, 1);  // O
        game4.makeMove(0, 2);  // X
        game4.makeMove(1, 2);  // O
        game4.makeMove(0, 3);  // X wins row 0!
        
        System.out.println("\n=== DEMO COMPLETE ===");
    }
}


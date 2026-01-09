package com.tictactoe.simple;

/**
 * Game - manages the game flow
 * 
 * Responsibilities:
 * - Track current player
 * - Coordinate moves
 * - Check win/draw conditions
 * - Announce results
 */
public class Game {
    private Board board;
    private Player[] players;
    private int currentPlayerIndex;
    private boolean gameOver;
    private Player winner;
    
    public Game(int boardSize, Player player1, Player player2) {
        this.board = new Board(boardSize);
        this.players = new Player[] { player1, player2 };
        this.currentPlayerIndex = 0;
        this.gameOver = false;
        this.winner = null;
    }
    
    /**
     * Make a move for the current player
     * @return true if move was valid, false otherwise
     */
    public boolean makeMove(int row, int col) {
        if (gameOver) {
            System.out.println("Game is already over!");
            return false;
        }
        
        Player current = players[currentPlayerIndex];
        Symbol symbol = current.getSymbol();
        
        // 1. Try to make the move
        if (!board.makeMove(row, col, symbol)) {
            System.out.println("Invalid move! Cell is occupied or out of bounds.");
            return false;
        }
        
        System.out.println(current.getName() + " placed " + symbol + " at (" + row + ", " + col + ")");
        board.print();
        
        // 2. Check for winner
        if (board.checkWinner(row, col, symbol)) {
            gameOver = true;
            winner = current;
            System.out.println("🎉 " + current.getName() + " wins!");
            return true;
        }
        
        // 3. Check for draw
        if (board.isFull()) {
            gameOver = true;
            System.out.println("It's a draw!");
            return true;
        }
        
        // 4. Switch to next player
        currentPlayerIndex = (currentPlayerIndex + 1) % 2;
        System.out.println("Next: " + players[currentPlayerIndex].getName() + 
                          " (" + players[currentPlayerIndex].getSymbol() + ")");
        
        return true;
    }
    
    // Getters
    public boolean isGameOver() { return gameOver; }
    public Player getWinner() { return winner; }
    public Player getCurrentPlayer() { return players[currentPlayerIndex]; }
    public Board getBoard() { return board; }
}


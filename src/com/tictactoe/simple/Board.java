package com.tictactoe.simple;

/**
 * Board - the game board with NxN cells
 * 
 * Key Features:
 * - NxN grid (configurable size)
 * - Move validation
 * - Win check (O(n) - scan row, col, diagonals)
 * - Draw detection
 */
public class Board {
    private int size;
    private Cell[][] grid;
    private int moveCount;
    
    public Board(int size) {
        this.size = size;
        this.moveCount = 0;
        this.grid = new Cell[size][size];
        
        // Initialize all cells
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = new Cell(i, j);
            }
        }
    }
    
    /**
     * Make a move at (row, col)
     * @return true if move was valid and made, false otherwise
     */
    public boolean makeMove(int row, int col, Symbol symbol) {
        // Validate bounds
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return false;
        }
        
        // Validate cell is empty
        if (!grid[row][col].isEmpty()) {
            return false;
        }
        
        // Make move
        grid[row][col].setSymbol(symbol);
        moveCount++;
        return true;
    }
    
    /**
     * Check if the last move at (row, col) resulted in a win
     * O(n) - check only affected row, column, and diagonals
     */
    public boolean checkWinner(int row, int col, Symbol symbol) {
        // 1. Check row
        if (checkRow(row, symbol)) return true;
        
        // 2. Check column
        if (checkCol(col, symbol)) return true;
        
        // 3. Check main diagonal (if cell is on it)
        if (row == col && checkDiagonal(symbol)) return true;
        
        // 4. Check anti-diagonal (if cell is on it)
        if (row + col == size - 1 && checkAntiDiagonal(symbol)) return true;
        
        return false;
    }
    
    private boolean checkRow(int row, Symbol symbol) {
        for (int c = 0; c < size; c++) {
            if (grid[row][c].getSymbol() != symbol) {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkCol(int col, Symbol symbol) {
        for (int r = 0; r < size; r++) {
            if (grid[r][col].getSymbol() != symbol) {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkDiagonal(Symbol symbol) {
        // Main diagonal: (0,0), (1,1), (2,2), ...
        for (int i = 0; i < size; i++) {
            if (grid[i][i].getSymbol() != symbol) {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkAntiDiagonal(Symbol symbol) {
        // Anti-diagonal: (0,2), (1,1), (2,0) for 3x3
        for (int i = 0; i < size; i++) {
            if (grid[i][size - 1 - i].getSymbol() != symbol) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if board is full (draw condition)
     */
    public boolean isFull() {
        return moveCount == size * size;
    }
    
    /**
     * Print the board
     */
    public void print() {
        System.out.println();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Symbol s = grid[i][j].getSymbol();
                System.out.print(s == null ? "." : s);
                if (j < size - 1) System.out.print(" | ");
            }
            System.out.println();
            if (i < size - 1) {
                for (int j = 0; j < size; j++) {
                    System.out.print("---");
                    if (j < size - 1) System.out.print("+");
                }
                System.out.println();
            }
        }
        System.out.println();
    }
    
    public int getSize() { return size; }
    public Cell getCell(int row, int col) { return grid[row][col]; }
}


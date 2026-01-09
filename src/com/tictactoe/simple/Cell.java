package com.tictactoe.simple;

/**
 * Cell - represents one cell on the board
 */
public class Cell {
    private int row;
    private int col;
    private Symbol symbol;
    
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.symbol = null;  // Empty initially
    }
    
    public int getRow() { return row; }
    public int getCol() { return col; }
    public Symbol getSymbol() { return symbol; }
    public void setSymbol(Symbol symbol) { this.symbol = symbol; }
    
    public boolean isEmpty() { 
        return symbol == null; 
    }
}


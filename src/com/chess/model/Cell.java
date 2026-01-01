package com.chess.model;

import com.chess.piece.Piece;

public class Cell {
    private int row;
    private int col;
    private Piece piece;
    
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.piece = null;
    }
    
    public boolean isEmpty() {
        return piece == null;
    }
    
    public int getRow() { return row; }
    public int getCol() { return col; }
    public Piece getPiece() { return piece; }
    public void setPiece(Piece piece) { this.piece = piece; }
    
    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}

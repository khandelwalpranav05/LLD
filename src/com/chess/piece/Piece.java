package com.chess.piece;

import com.chess.model.Cell;
import com.chess.model.Color;
import com.chess.model.PieceType;
import com.chess.service.Board;

public abstract class Piece {
    protected Color color;
    protected boolean hasMoved;
    
    public Piece(Color color) {
        this.color = color;
        this.hasMoved = false;
    }
    
    public abstract boolean canMove(Board board, Cell start, Cell end);
    public abstract PieceType getType();
    public abstract String getSymbol();
    
    public Color getColor() { return color; }
    public boolean hasMoved() { return hasMoved; }
    public void setMoved(boolean moved) { this.hasMoved = moved; }
    
    // Helper: Check if path is clear (for Rook, Bishop, Queen)
    protected boolean isPathClear(Board board, Cell start, Cell end) {
        int rowDir = Integer.signum(end.getRow() - start.getRow());
        int colDir = Integer.signum(end.getCol() - start.getCol());
        
        int row = start.getRow() + rowDir;
        int col = start.getCol() + colDir;
        
        while (row != end.getRow() || col != end.getCol()) {
            if (!board.getCell(row, col).isEmpty()) {
                return false;
            }
            row += rowDir;
            col += colDir;
        }
        return true;
    }
}

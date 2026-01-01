package com.chess.piece;

import com.chess.model.Cell;
import com.chess.model.Color;
import com.chess.model.PieceType;
import com.chess.service.Board;

public class Pawn extends Piece {
    public Pawn(Color color) {
        super(color);
    }
    
    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        int direction = (color == Color.WHITE) ? 1 : -1; // White moves up, black down
        int rowDiff = end.getRow() - start.getRow();
        int colDiff = Math.abs(end.getCol() - start.getCol());
        
        // Forward move (no capture)
        if (colDiff == 0 && end.isEmpty()) {
            if (rowDiff == direction) return true; // 1 step
            if (!hasMoved && rowDiff == 2 * direction) { // 2 steps from start
                Cell middle = board.getCell(start.getRow() + direction, start.getCol());
                return middle.isEmpty();
            }
        }
        
        // Diagonal capture
        if (colDiff == 1 && rowDiff == direction && !end.isEmpty()) {
            return end.getPiece().getColor() != this.color;
        }
        
        return false;
    }
    
    @Override
    public PieceType getType() { return PieceType.PAWN; }
    
    @Override
    public String getSymbol() { return color == Color.WHITE ? "P" : "p"; }
}

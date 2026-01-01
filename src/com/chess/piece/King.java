package com.chess.piece;

import com.chess.model.Cell;
import com.chess.model.Color;
import com.chess.model.PieceType;
import com.chess.service.Board;

public class King extends Piece {
    public King(Color color) {
        super(color);
    }
    
    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        int rowDiff = Math.abs(end.getRow() - start.getRow());
        int colDiff = Math.abs(end.getCol() - start.getCol());
        
        // King moves 1 square in any direction
        return rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff > 0);
    }
    
    @Override
    public PieceType getType() { return PieceType.KING; }
    
    @Override
    public String getSymbol() { return color == Color.WHITE ? "K" : "k"; }
}

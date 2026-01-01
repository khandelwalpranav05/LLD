package com.chess.piece;

import com.chess.model.Cell;
import com.chess.model.Color;
import com.chess.model.PieceType;
import com.chess.service.Board;

public class Knight extends Piece {
    public Knight(Color color) {
        super(color);
    }
    
    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        int rowDiff = Math.abs(end.getRow() - start.getRow());
        int colDiff = Math.abs(end.getCol() - start.getCol());
        
        // Knight: L-shape (2+1 or 1+2)
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }
    
    @Override
    public PieceType getType() { return PieceType.KNIGHT; }
    
    @Override
    public String getSymbol() { return color == Color.WHITE ? "N" : "n"; }
}

package com.chess.piece;

import com.chess.model.Cell;
import com.chess.model.Color;
import com.chess.model.PieceType;
import com.chess.service.Board;

public class Bishop extends Piece {
    public Bishop(Color color) {
        super(color);
    }
    
    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        int rowDiff = Math.abs(end.getRow() - start.getRow());
        int colDiff = Math.abs(end.getCol() - start.getCol());
        
        // Bishop: Diagonal only
        return rowDiff == colDiff && rowDiff > 0 && isPathClear(board, start, end);
    }
    
    @Override
    public PieceType getType() { return PieceType.BISHOP; }
    
    @Override
    public String getSymbol() { return color == Color.WHITE ? "B" : "b"; }
}

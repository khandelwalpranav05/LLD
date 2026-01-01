package com.chess.piece;

import com.chess.model.Cell;
import com.chess.model.Color;
import com.chess.model.PieceType;
import com.chess.service.Board;

public class Queen extends Piece {
    public Queen(Color color) {
        super(color);
    }
    
    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        int rowDiff = Math.abs(end.getRow() - start.getRow());
        int colDiff = Math.abs(end.getCol() - start.getCol());
        
        // Queen: Rook + Bishop movement
        boolean straightLine = (start.getRow() == end.getRow() || start.getCol() == end.getCol());
        boolean diagonal = (rowDiff == colDiff);
        
        return (straightLine || diagonal) && isPathClear(board, start, end);
    }
    
    @Override
    public PieceType getType() { return PieceType.QUEEN; }
    
    @Override
    public String getSymbol() { return color == Color.WHITE ? "Q" : "q"; }
}

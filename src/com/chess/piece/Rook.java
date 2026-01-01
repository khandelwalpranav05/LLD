package com.chess.piece;

import com.chess.model.Cell;
import com.chess.model.Color;
import com.chess.model.PieceType;
import com.chess.service.Board;

public class Rook extends Piece {
    public Rook(Color color) {
        super(color);
    }
    
    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        // Rook: Straight lines only
        boolean straightLine = (start.getRow() == end.getRow() || start.getCol() == end.getCol());
        return straightLine && isPathClear(board, start, end);
    }
    
    @Override
    public PieceType getType() { return PieceType.ROOK; }
    
    @Override
    public String getSymbol() { return color == Color.WHITE ? "R" : "r"; }
}

package com.chess.model;

import com.chess.piece.Piece;
import com.chess.service.Board;

// Command Pattern: Move is a command with execute() and undo()
public class Move {
    private Piece piece;
    private Cell startCell;
    private Cell endCell;
    private Piece capturedPiece;
    private boolean wasFirstMove;
    
    public Move(Piece piece, Cell start, Cell end) {
        this.piece = piece;
        this.startCell = start;
        this.endCell = end;
        this.capturedPiece = end.getPiece();
        this.wasFirstMove = !piece.hasMoved();
    }
    
    public void execute(Board board) {
        capturedPiece = endCell.getPiece();
        endCell.setPiece(piece);
        startCell.setPiece(null);
        piece.setMoved(true);
    }
    
    public void undo(Board board) {
        startCell.setPiece(piece);
        endCell.setPiece(capturedPiece);
        if (wasFirstMove) {
            piece.setMoved(false);
        }
    }
    
    public Piece getPiece() { return piece; }
    public Cell getStartCell() { return startCell; }
    public Cell getEndCell() { return endCell; }
    public Piece getCapturedPiece() { return capturedPiece; }
    
    @Override
    public String toString() {
        return piece.getSymbol() + " " + startCell + " -> " + endCell;
    }
}

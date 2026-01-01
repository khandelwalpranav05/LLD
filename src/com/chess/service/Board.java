package com.chess.service;

import com.chess.model.Cell;
import com.chess.model.Color;
import com.chess.piece.*;

public class Board {
    private Cell[][] cells;
    public static final int SIZE = 8;
    
    public Board() {
        cells = new Cell[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }
    }
    
    public void initialize() {
        // Place White pieces (rows 0-1)
        placePieces(0, Color.WHITE);
        for (int col = 0; col < SIZE; col++) {
            cells[1][col].setPiece(new Pawn(Color.WHITE));
        }
        
        // Place Black pieces (rows 6-7)
        placePieces(7, Color.BLACK);
        for (int col = 0; col < SIZE; col++) {
            cells[6][col].setPiece(new Pawn(Color.BLACK));
        }
    }
    
    private void placePieces(int row, Color color) {
        cells[row][0].setPiece(new Rook(color));
        cells[row][1].setPiece(new Knight(color));
        cells[row][2].setPiece(new Bishop(color));
        cells[row][3].setPiece(new Queen(color));
        cells[row][4].setPiece(new King(color));
        cells[row][5].setPiece(new Bishop(color));
        cells[row][6].setPiece(new Knight(color));
        cells[row][7].setPiece(new Rook(color));
    }
    
    public Cell getCell(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return null;
        return cells[row][col];
    }
    
    public Cell findKing(Color color) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Piece p = cells[i][j].getPiece();
                if (p != null && p.getType() == com.chess.model.PieceType.KING && p.getColor() == color) {
                    return cells[i][j];
                }
            }
        }
        return null;
    }
    
    public boolean isSquareUnderAttack(int row, int col, Color attackerColor) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Piece p = cells[i][j].getPiece();
                if (p != null && p.getColor() == attackerColor) {
                    if (p.canMove(this, cells[i][j], cells[row][col])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public void printBoard() {
        System.out.println("  a b c d e f g h");
        for (int row = SIZE - 1; row >= 0; row--) {
            System.out.print((row + 1) + " ");
            for (int col = 0; col < SIZE; col++) {
                Piece p = cells[row][col].getPiece();
                System.out.print((p == null ? "." : p.getSymbol()) + " ");
            }
            System.out.println((row + 1));
        }
        System.out.println("  a b c d e f g h");
    }
}

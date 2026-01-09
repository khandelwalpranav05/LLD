package com.chess.service;

import com.chess.model.*;
import com.chess.piece.Piece;
import java.util.Stack;

public class Game {
    private Board board;
    private Player[] players;
    private int currentPlayerIndex;
    private GameStatus status;
    private Stack<Move> moveHistory;
    
    public Game(Player p1, Player p2) {
        this.board = new Board();
        this.players = new Player[]{p1, p2};
        this.currentPlayerIndex = 0; // White starts
        this.status = GameStatus.IN_PROGRESS;
        this.moveHistory = new Stack<>();
    }
    
    public void start() {
        board.initialize();
        System.out.println("Game Started!");
        board.printBoard();
    }
    
    public synchronized boolean makeMove(int startRow, int startCol, int endRow, int endCol) {
        if (status != GameStatus.IN_PROGRESS) {
            System.out.println("Game is over!");
            return false;
        }
        
        Cell start = board.getCell(startRow, startCol);
        Cell end = board.getCell(endRow, endCol);
        
        if (start == null || end == null) {
            System.out.println("Invalid coordinates.");
            return false;
        }
        
        Piece piece = start.getPiece();
        if (piece == null) {
            System.out.println("No piece at start position.");
            return false;
        }
        
        Player current = players[currentPlayerIndex];
        if (piece.getColor() != current.getColor()) {
            System.out.println("It's " + current.getName() + "'s turn (" + current.getColor() + ").");
            return false;
        }
        
        // Validate move
        if (!piece.canMove(board, start, end)) {
            System.out.println("Invalid move for " + piece.getType());
            return false;
        }
        
        // Check if destination has own piece
        if (end.getPiece() != null && end.getPiece().getColor() == piece.getColor()) {
            System.out.println("Cannot capture your own piece.");
            return false;
        }
        
        // Create and simulate move
        Move move = new Move(piece, start, end);
        move.execute(board);
        
        // NEW: Check if this move leaves OUR king in check (ILLEGAL!)
        if (isInCheck(current.getColor())) {
            move.undo(board); // Rollback the move
            System.out.println("Illegal move: would leave your King in check!");
            return false;
        }
        
        // Move is legal - add to history
        moveHistory.push(move);
        
        System.out.println(current.getName() + " played: " + move);
        board.printBoard();
        
        // Check for checkmate/stalemate on opponent
        Color opponent = current.getColor().opposite();
        if (isInCheck(opponent)) {
            if (isCheckmate(opponent)) {
                status = (current.getColor() == Color.WHITE) ? GameStatus.WHITE_WINS : GameStatus.BLACK_WINS;
                System.out.println("CHECKMATE! " + current.getName() + " wins!");
            } else {
                System.out.println("CHECK!");
            }
        } else if (isStalemate(opponent)) {
            status = GameStatus.STALEMATE;
            System.out.println("STALEMATE! Game is a draw.");
        }
        
        // Switch turn
        currentPlayerIndex = 1 - currentPlayerIndex;
        return true;
    }
    
    public synchronized boolean undoLastMove() {
        if (moveHistory.isEmpty()) {
            System.out.println("No moves to undo.");
            return false;
        }
        Move last = moveHistory.pop();
        last.undo(board);
        currentPlayerIndex = 1 - currentPlayerIndex;
        System.out.println("Undid move: " + last);
        board.printBoard();
        return true;
    }
    
    public boolean isInCheck(Color color) {
        Cell kingCell = board.findKing(color);
        return board.isSquareUnderAttack(kingCell.getRow(), kingCell.getCol(), color.opposite());
    }
    
    /**
     * Checkmate: King is in check AND no legal move can get out of check.
     * We try every possible move for the player and see if any escapes check.
     */
    public boolean isCheckmate(Color color) {
        if (!isInCheck(color)) {
            return false; // Can't be checkmate if not in check
        }
        return !hasAnyLegalMove(color);
    }
    
    /**
     * Stalemate: King is NOT in check but no legal moves exist.
     */
    public boolean isStalemate(Color color) {
        if (isInCheck(color)) {
            return false; // If in check, it's not stalemate
        }
        return !hasAnyLegalMove(color);
    }
    
    /**
     * Check if the player has any legal move available.
     * A legal move is one that:
     * 1. Is valid according to piece movement rules
     * 2. Doesn't leave the king in check
     */
    private boolean hasAnyLegalMove(Color color) {
        // Try every piece of this color
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Cell startCell = board.getCell(row, col);
                Piece piece = startCell.getPiece();
                
                if (piece == null || piece.getColor() != color) {
                    continue;
                }
                
                // Try every possible destination
                for (int endRow = 0; endRow < Board.SIZE; endRow++) {
                    for (int endCol = 0; endCol < Board.SIZE; endCol++) {
                        Cell endCell = board.getCell(endRow, endCol);
                        
                        // Skip if can't move there
                        if (!piece.canMove(board, startCell, endCell)) {
                            continue;
                        }
                        
                        // Skip if capturing own piece
                        if (endCell.getPiece() != null && 
                            endCell.getPiece().getColor() == color) {
                            continue;
                        }
                        
                        // Try the move
                        Move testMove = new Move(piece, startCell, endCell);
                        testMove.execute(board);
                        
                        boolean stillInCheck = isInCheck(color);
                        
                        // Undo the test move
                        testMove.undo(board);
                        
                        if (!stillInCheck) {
                            // Found a legal move that escapes check!
                            return true;
                        }
                    }
                }
            }
        }
        
        // No legal moves found
        return false;
    }
    
    public GameStatus getStatus() { return status; }
    public Board getBoard() { return board; }
}

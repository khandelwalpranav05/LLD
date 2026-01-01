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
    
    public boolean isCheckmate(Color color) {
        // Simplified: If in check and king can't move, it's checkmate
        // Full implementation would check all possible moves
        return isInCheck(color); // Placeholder
    }
    
    public GameStatus getStatus() { return status; }
    public Board getBoard() { return board; }
}

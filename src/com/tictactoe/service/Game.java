package com.tictactoe.service;

import com.tictactoe.model.*;
import com.tictactoe.strategy.WinningStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Game {
    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;
    private GameStatus status;
    private WinningStrategy winningStrategy;
    
    // Memento: Stack to store history
    private Stack<BoardMemento> history;

    public Game(int size, List<Player> players, WinningStrategy winningStrategy) {
        this.board = new Board(size);
        this.players = players;
        this.winningStrategy = winningStrategy;
        this.currentPlayerIndex = 0;
        this.status = GameStatus.IN_PROGRESS;
        this.history = new Stack<>();
    }

    public void makeMove() {
        Player currentPlayer = players.get(currentPlayerIndex);
        
        // 1. Save State (Memento)
        history.push(board.createMemento());

        // 2. Decide Move
        Cell proposedCell = currentPlayer.decideMove(board);
        
        // 3. Validate Move
        if (!validateMove(proposedCell)) {
            System.out.println("Invalid Move! Try again.");
            history.pop(); // Revert history push
            return;
        }

        // 4. Apply Move
        Cell cellOnBoard = board.getCells().get(proposedCell.getRow()).get(proposedCell.getCol());
        cellOnBoard.setSymbol(currentPlayer.getSymbol());
        Move move = new Move(cellOnBoard, currentPlayer);
        
        board.printBoard();

        // 5. Check Winner
        if (winningStrategy.checkWinner(board, move)) {
            status = GameStatus.ENDED;
            System.out.println("Winner is " + currentPlayer.getName());
            return;
        }

        // 6. Check Draw
        if (history.size() == board.getSize() * board.getSize()) {
            status = GameStatus.DRAW;
            System.out.println("Game Draw!");
            return;
        }

        // 7. Next Turn
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public void undo() {
        if (history.isEmpty()) {
            System.out.println("Nothing to undo!");
            return;
        }
        
        // Restore State
        BoardMemento memento = history.pop();
        board.restore(memento);
        
        // Revert Turn
        currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        System.out.println("Undo successful. Back to " + players.get(currentPlayerIndex).getName());
        board.printBoard();
    }

    private boolean validateMove(Cell cell) {
        int r = cell.getRow();
        int c = cell.getCol();
        if (r < 0 || r >= board.getSize() || c < 0 || c >= board.getSize()) return false;
        return board.getCells().get(r).get(c).isEmpty();
    }

    public GameStatus getStatus() { return status; }
}

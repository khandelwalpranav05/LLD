package com.tictactoe.strategy;

import com.tictactoe.model.Board;
import com.tictactoe.model.Move;
import com.tictactoe.model.Symbol;
import java.util.HashMap;
import java.util.Map;

/**
 * O(1) Winning Strategy - Explained Simply
 * 
 * THE IDEA:
 * Instead of scanning the board after every move, we just count!
 * 
 * - For each row: track how many X's and O's are in it
 * - For each column: track how many X's and O's are in it  
 * - For each diagonal: track how many X's and O's are in it
 * 
 * When any count reaches board size → that player wins!
 * 
 * WHY O(1)?
 * Each move only updates 4 counters max (1 row + 1 col + 2 diagonals).
 * No matter how big the board is, we always do the same amount of work.
 */
public class OrderOneWinningStrategy implements WinningStrategy {
    
    private final int size;
    
    // rowCounts[row][symbol] = count of that symbol in this row
    private final Map<Integer, Map<Symbol, Integer>> rowCounts = new HashMap<>();
    
    // colCounts[col][symbol] = count of that symbol in this column
    private final Map<Integer, Map<Symbol, Integer>> colCounts = new HashMap<>();
    
    // Diagonal from top-left to bottom-right (where row == col)
    private final Map<Symbol, Integer> leftDiagonalCounts = new HashMap<>();
    
    // Diagonal from top-right to bottom-left (where row + col == size - 1)
    private final Map<Symbol, Integer> rightDiagonalCounts = new HashMap<>();

    public OrderOneWinningStrategy(int size) {
        this.size = size;
    }

    @Override
    public boolean checkWinner(Board board, Move lastMove) {
        int row = lastMove.getCell().getRow();
        int col = lastMove.getCell().getCol();
        Symbol symbol = lastMove.getPlayer().getSymbol();

        // 1. Update and check ROW count
        if (incrementAndCheck(rowCounts, row, symbol)) {
            return true;
        }

        // 2. Update and check COLUMN count
        if (incrementAndCheck(colCounts, col, symbol)) {
            return true;
        }

        // 3. Update and check LEFT DIAGONAL (cells where row == col)
        //    Example: (0,0), (1,1), (2,2) for a 3x3 board
        if (row == col) {
            if (incrementAndCheck(leftDiagonalCounts, symbol)) {
                return true;
            }
        }

        // 4. Update and check RIGHT DIAGONAL (cells where row + col == size - 1)
        //    Example: (0,2), (1,1), (2,0) for a 3x3 board
        if (row + col == size - 1) {
            if (incrementAndCheck(rightDiagonalCounts, symbol)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Helper: Increment count for a row/column and check if player won.
     */
    private boolean incrementAndCheck(Map<Integer, Map<Symbol, Integer>> counts, int index, Symbol symbol) {
        counts.putIfAbsent(index, new HashMap<>());
        Map<Symbol, Integer> symbolCounts = counts.get(index);
        
        int newCount = symbolCounts.getOrDefault(symbol, 0) + 1;
        symbolCounts.put(symbol, newCount);
        
        return newCount == size;
    }

    /**
     * Helper: Increment count for a diagonal and check if player won.
     */
    private boolean incrementAndCheck(Map<Symbol, Integer> diagonalCounts, Symbol symbol) {
        int newCount = diagonalCounts.getOrDefault(symbol, 0) + 1;
        diagonalCounts.put(symbol, newCount);
        
        return newCount == size;
    }
}

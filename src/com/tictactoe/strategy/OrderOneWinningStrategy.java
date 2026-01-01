package com.tictactoe.strategy;

import com.tictactoe.model.Board;
import com.tictactoe.model.Move;
import com.tictactoe.model.Symbol;
import java.util.HashMap;
import java.util.Map;

/**
 * O(1) Winning Strategy.
 * Keeps track of counts for each row, col, and diagonal.
 * Note: This implementation assumes the strategy object persists for the game.
 */
public class OrderOneWinningStrategy implements WinningStrategy {
    private int size;
    private Map<Integer, Map<Symbol, Integer>> rowCounts = new HashMap<>();
    private Map<Integer, Map<Symbol, Integer>> colCounts = new HashMap<>();
    private Map<Symbol, Integer> leftDiagCounts = new HashMap<>();
    private Map<Symbol, Integer> rightDiagCounts = new HashMap<>();

    public OrderOneWinningStrategy(int size) {
        this.size = size;
    }

    @Override
    public boolean checkWinner(Board board, Move lastMove) {
        int row = lastMove.getCell().getRow();
        int col = lastMove.getCell().getCol();
        Symbol symbol = lastMove.getPlayer().getSymbol();

        // Update Row
        rowCounts.putIfAbsent(row, new HashMap<>());
        Map<Symbol, Integer> rMap = rowCounts.get(row);
        rMap.put(symbol, rMap.getOrDefault(symbol, 0) + 1);
        if (rMap.get(symbol) == size) return true;

        // Update Col
        colCounts.putIfAbsent(col, new HashMap<>());
        Map<Symbol, Integer> cMap = colCounts.get(col);
        cMap.put(symbol, cMap.getOrDefault(symbol, 0) + 1);
        if (cMap.get(symbol) == size) return true;

        // Update Left Diag (row == col)
        if (row == col) {
            leftDiagCounts.put(symbol, leftDiagCounts.getOrDefault(symbol, 0) + 1);
            if (leftDiagCounts.get(symbol) == size) return true;
        }

        // Update Right Diag (row + col == size - 1)
        if (row + col == size - 1) {
            rightDiagCounts.put(symbol, rightDiagCounts.getOrDefault(symbol, 0) + 1);
            if (rightDiagCounts.get(symbol) == size) return true;
        }

        return false;
    }
}

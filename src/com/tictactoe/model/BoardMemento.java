package com.tictactoe.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Memento Pattern: Stores a snapshot of the board state.
 */
public class BoardMemento {
    private final List<List<Symbol>> gridSnapshot;

    public BoardMemento(List<List<Cell>> cells) {
        this.gridSnapshot = new ArrayList<>();
        for (List<Cell> row : cells) {
            List<Symbol> rowSymbols = new ArrayList<>();
            for (Cell cell : row) {
                rowSymbols.add(cell.getSymbol());
            }
            gridSnapshot.add(rowSymbols);
        }
    }

    public List<List<Symbol>> getGridSnapshot() {
        return gridSnapshot;
    }
}

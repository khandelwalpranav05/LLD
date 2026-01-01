package com.tictactoe.model;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private int size;
    private List<List<Cell>> cells;

    public Board(int size) {
        this.size = size;
        this.cells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            List<Cell> row = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                row.add(new Cell(i, j));
            }
            cells.add(row);
        }
    }

    public int getSize() { return size; }
    public List<List<Cell>> getCells() { return cells; }

    public void applyMove(Move move) {
        int r = move.getCell().getRow();
        int c = move.getCell().getCol();
        cells.get(r).get(c).setSymbol(move.getPlayer().getSymbol());
    }

    // Memento: Create Snapshot
    public BoardMemento createMemento() {
        return new BoardMemento(cells);
    }

    // Memento: Restore Snapshot
    public void restore(BoardMemento memento) {
        List<List<Symbol>> snapshot = memento.getGridSnapshot();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells.get(i).get(j).setSymbol(snapshot.get(i).get(j));
            }
        }
    }

    public void printBoard() {
        for (List<Cell> row : cells) {
            for (Cell cell : row) {
                System.out.print((cell.getSymbol() == null ? "-" : cell.getSymbol()) + " ");
            }
            System.out.println();
        }
    }
}

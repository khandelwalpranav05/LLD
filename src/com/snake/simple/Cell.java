package com.snake.simple;

import java.util.Objects;

/**
 * Cell - Immutable coordinate pair
 * MUST override equals/hashCode for HashSet to work!
 */
public class Cell {
    public final int row, col;
    
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    // Move in a direction
    public Cell move(Direction d) {
        return new Cell(row + d.dr, col + d.dc);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cell)) return false;
        Cell c = (Cell) o;
        return row == c.row && col == c.col;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
    
    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}


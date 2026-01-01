package com.snakeladder.model;

import java.util.HashMap;
import java.util.Map;

public class Board {
    private int size; // Total cells, e.g., 100
    private Map<Integer, Jump> jumps; // Position -> Jump

    public Board(int size) {
        this.size = size;
        this.jumps = new HashMap<>();
    }

    public void addJump(int start, int end) {
        // Validation: start and end within bounds
        if (start < 1 || start >= size || end < 1 || end > size) {
            throw new IllegalArgumentException("Jump positions out of bounds");
        }
        jumps.put(start, new Jump(start, end));
    }

    public Jump getJump(int position) {
        return jumps.get(position);
    }

    public int getSize() { return size; }
}

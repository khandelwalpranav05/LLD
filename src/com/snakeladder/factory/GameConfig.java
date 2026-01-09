package com.snakeladder.factory;

import com.snakeladder.strategy.DiceStrategy;
import com.snakeladder.strategy.NormalDiceStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration object for custom game creation.
 * Uses Builder pattern for fluent configuration.
 * 
 * Example usage:
 *   GameConfig config = new GameConfig.Builder()
 *       .boardSize(100)
 *       .addPlayers("Alice", "Bob")
 *       .addSnake(99, 10)
 *       .addLadder(2, 25)
 *       .build();
 */
public class GameConfig {
    private final int boardSize;
    private final List<String> playerNames;
    private final List<int[]> snakes;
    private final List<int[]> ladders;
    private final DiceStrategy diceStrategy;

    private GameConfig(Builder builder) {
        this.boardSize = builder.boardSize;
        this.playerNames = builder.playerNames;
        this.snakes = builder.snakes;
        this.ladders = builder.ladders;
        this.diceStrategy = builder.diceStrategy;
    }

    public int getBoardSize() { return boardSize; }
    public List<String> getPlayerNames() { return playerNames; }
    public List<int[]> getSnakes() { return snakes; }
    public List<int[]> getLadders() { return ladders; }
    public DiceStrategy getDiceStrategy() { return diceStrategy; }

    /**
     * Builder for fluent GameConfig creation.
     */
    public static class Builder {
        private int boardSize = 100;  // Default
        private List<String> playerNames = new ArrayList<>();
        private List<int[]> snakes = new ArrayList<>();
        private List<int[]> ladders = new ArrayList<>();
        private DiceStrategy diceStrategy = new NormalDiceStrategy();

        public Builder boardSize(int size) {
            this.boardSize = size;
            return this;
        }

        public Builder addPlayer(String name) {
            this.playerNames.add(name);
            return this;
        }

        public Builder addPlayers(String... names) {
            for (String name : names) {
                this.playerNames.add(name);
            }
            return this;
        }

        public Builder addSnake(int head, int tail) {
            if (head <= tail) {
                throw new IllegalArgumentException("Snake head must be above tail: " + head + " -> " + tail);
            }
            this.snakes.add(new int[]{head, tail});
            return this;
        }

        public Builder addLadder(int bottom, int top) {
            if (bottom >= top) {
                throw new IllegalArgumentException("Ladder bottom must be below top: " + bottom + " -> " + top);
            }
            this.ladders.add(new int[]{bottom, top});
            return this;
        }

        public Builder diceStrategy(DiceStrategy strategy) {
            this.diceStrategy = strategy;
            return this;
        }

        public GameConfig build() {
            return new GameConfig(this);
        }
    }
}


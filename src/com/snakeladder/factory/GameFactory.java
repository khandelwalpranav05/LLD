package com.snakeladder.factory;

import com.snakeladder.model.Board;
import com.snakeladder.model.Player;
import com.snakeladder.service.Game;
import com.snakeladder.strategy.DiceStrategy;
import com.snakeladder.strategy.NormalDiceStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating Snake & Ladder games.
 * 
 * WHY FACTORY PATTERN HERE?
 * 
 * 1. ENCAPSULATION: Hides the complexity of board setup from clients
 * 2. PRESETS: Offers ready-made configurations (classic, easy, hard)
 * 3. VALIDATION: Single place to ensure valid game configurations
 * 4. FLEXIBILITY: Easy to add new game modes without changing client code
 * 5. TESTABILITY: Can create test-friendly games with known configurations
 */
public class GameFactory {

    /**
     * Create a classic 10x10 (100 cell) Snake & Ladder game.
     * Standard snakes and ladders configuration.
     */
    public static Game createClassicGame(List<String> playerNames) {
        Board board = new Board(100);
        
        // Classic Snakes (go down)
        board.addJump(99, 10);  // Near-win snake - adds tension!
        board.addJump(95, 72);
        board.addJump(62, 18);
        board.addJump(50, 5);
        board.addJump(35, 6);
        
        // Classic Ladders (go up)
        board.addJump(2, 25);
        board.addJump(8, 34);
        board.addJump(20, 77);
        board.addJump(40, 89);
        board.addJump(71, 92);
        
        return buildGame(board, playerNames, new NormalDiceStrategy());
    }

    /**
     * Create an easy game - more ladders, fewer snakes.
     * Good for kids or quick games.
     */
    public static Game createEasyGame(List<String> playerNames) {
        Board board = new Board(100);
        
        // Few snakes
        board.addJump(50, 25);
        board.addJump(80, 60);
        
        // Many ladders
        board.addJump(3, 38);
        board.addJump(10, 45);
        board.addJump(28, 76);
        board.addJump(42, 85);
        board.addJump(55, 95);
        
        return buildGame(board, playerNames, new NormalDiceStrategy());
    }

    /**
     * Create a hard game - more snakes, fewer ladders.
     * For competitive players who want a challenge.
     */
    public static Game createHardGame(List<String> playerNames) {
        Board board = new Board(100);
        
        // Many snakes
        board.addJump(99, 7);   // Brutal near-end snake
        board.addJump(95, 24);
        board.addJump(87, 36);
        board.addJump(62, 19);
        board.addJump(54, 12);
        board.addJump(46, 5);
        board.addJump(32, 3);
        
        // Few ladders
        board.addJump(4, 28);
        board.addJump(21, 42);
        
        return buildGame(board, playerNames, new NormalDiceStrategy());
    }

    /**
     * Create a custom game with specific configuration.
     * For advanced users who want full control.
     */
    public static Game createCustomGame(GameConfig config) {
        validateConfig(config);
        
        Board board = new Board(config.getBoardSize());
        
        // Add snakes
        for (int[] snake : config.getSnakes()) {
            board.addJump(snake[0], snake[1]);
        }
        
        // Add ladders
        for (int[] ladder : config.getLadders()) {
            board.addJump(ladder[0], ladder[1]);
        }
        
        return buildGame(board, config.getPlayerNames(), config.getDiceStrategy());
    }

    /**
     * Create a test-friendly game with predictable configuration.
     * Useful for unit testing.
     */
    public static Game createTestGame(List<String> playerNames, DiceStrategy diceStrategy) {
        Board board = new Board(20);  // Small board for quick tests
        
        board.addJump(5, 15);   // One ladder
        board.addJump(18, 3);   // One snake
        
        return buildGame(board, playerNames, diceStrategy);
    }

    // ==================== PRIVATE HELPERS ====================

    private static Game buildGame(Board board, List<String> playerNames, DiceStrategy diceStrategy) {
        List<Player> players = new ArrayList<>();
        for (String name : playerNames) {
            players.add(new Player(name));
        }
        return new Game(board, players, diceStrategy);
    }

    private static void validateConfig(GameConfig config) {
        if (config.getPlayerNames() == null || config.getPlayerNames().isEmpty()) {
            throw new IllegalArgumentException("At least one player required");
        }
        if (config.getBoardSize() < 10) {
            throw new IllegalArgumentException("Board size must be at least 10");
        }
        // Validate no overlapping snakes and ladders
        validateNoOverlaps(config);
    }

    private static void validateNoOverlaps(GameConfig config) {
        java.util.Set<Integer> startPositions = new java.util.HashSet<>();
        
        for (int[] snake : config.getSnakes()) {
            if (!startPositions.add(snake[0])) {
                throw new IllegalArgumentException("Overlapping jump at position " + snake[0]);
            }
        }
        for (int[] ladder : config.getLadders()) {
            if (!startPositions.add(ladder[0])) {
                throw new IllegalArgumentException("Overlapping jump at position " + ladder[0]);
            }
        }
    }
}


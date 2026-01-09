package com.snakeladder;

import com.snakeladder.factory.GameConfig;
import com.snakeladder.factory.GameFactory;
import com.snakeladder.service.Game;

import java.util.List;

public class SnakeLadderDemo {
    public static void main(String[] args) {
        // ==================== OPTION 1: Use Preset (Recommended) ====================
        // One line! All complexity hidden in factory.
        Game game = GameFactory.createClassicGame(List.of("Alice", "Bob"));
        
        // Other presets available:
        // Game easyGame = GameFactory.createEasyGame(List.of("Alice", "Bob"));
        // Game hardGame = GameFactory.createHardGame(List.of("Alice", "Bob"));
        
        // ==================== OPTION 2: Custom Configuration ====================
        // For when you need full control, but still validated and clean
        /*
        GameConfig config = new GameConfig.Builder()
                .boardSize(100)
                .addPlayers("Alice", "Bob", "Charlie")
                .addSnake(99, 10)    // Builder validates: head > tail
                .addSnake(50, 5)
                .addLadder(2, 25)    // Builder validates: bottom < top
                .addLadder(40, 89)
                .build();
        
        Game game = GameFactory.createCustomGame(config);
        */
        
        System.out.println("Game Created. Waiting for requests...");

        // Simulate a Client sending requests
        // In a real server, these would be HTTP requests coming in random order
        int turn = 0;
        while (true) {
            String playerRequesting = (turn % 2 == 0) ? "Alice" : "Bob";
            String response = game.makeMove(playerRequesting);
            System.out.println("[Response]: " + response);
            
            if (response.contains("WINNER")) break;
            
            turn++;
            // Simulate network delay
            try { Thread.sleep(100); } catch (InterruptedException e) { }
        }
    }
}

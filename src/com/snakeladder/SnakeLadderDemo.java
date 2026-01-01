package com.snakeladder;

import com.snakeladder.model.Board;
import com.snakeladder.model.Player;
import com.snakeladder.service.Game;
import com.snakeladder.strategy.NormalDiceStrategy;

import java.util.ArrayList;
import java.util.List;

public class SnakeLadderDemo {
    public static void main(String[] args) {
        // 1. Setup Board
        Board board = new Board(100);
        
        // Add Snakes (Down)
        board.addJump(99, 10);
        board.addJump(50, 5);
        
        // Add Ladders (Up)
        board.addJump(2, 25);
        board.addJump(40, 89);

        // 2. Setup Players
        List<Player> players = new ArrayList<>();
        players.add(new Player("Alice"));
        players.add(new Player("Bob"));

        // 3. Start Game (Simulating Server Requests)
        Game game = new Game(board, players, new NormalDiceStrategy());
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

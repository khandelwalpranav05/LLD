package com.tictactoe;

import com.tictactoe.model.*;
import com.tictactoe.service.Game;
import com.tictactoe.strategy.OrderOneWinningStrategy;
import com.tictactoe.factory.PlayerFactory;
import java.util.ArrayList;
import java.util.List;

public class TicTacToeDemo {
    public static void main(String[] args) {
        System.out.println("Starting Tic Tac Toe...");

        // 1. Setup Players (Bot vs Bot for auto-demo)
        // 1. Setup Players using Factory
        List<Player> players = new ArrayList<>();
        players.add(PlayerFactory.createPlayer(PlayerType.BOT, "Bot-X", Symbol.X));
        players.add(PlayerFactory.createPlayer(PlayerType.BOT, "Bot-O", Symbol.O));

        // 2. Setup Game
        Game game = new Game(3, players, new OrderOneWinningStrategy(3));

        // 3. Play
        // Move 1 (X)
        game.makeMove(); 
        
        // Move 2 (O)
        game.makeMove();

        // Undo Test
        System.out.println("--- Testing Undo ---");
        game.undo(); // Should revert O's move
        
        // Resume Game
        while (game.getStatus() == GameStatus.IN_PROGRESS) {
            game.makeMove();
        }
    }
}

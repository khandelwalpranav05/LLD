package com.chess;

import com.chess.model.*;
import com.chess.service.Game;

public class ChessDemo {
    public static void main(String[] args) {
        Player white = new Player("Alice", Color.WHITE);
        Player black = new Player("Bob", Color.BLACK);
        
        Game game = new Game(white, black);
        game.start();
        
        // Simulate a few moves
        System.out.println("\n--- Making Moves ---");
        game.makeMove(1, 4, 3, 4); // White: e2 -> e4
        game.makeMove(6, 4, 4, 4); // Black: e7 -> e5
        game.makeMove(0, 5, 3, 2); // White: Bishop f1 -> c4
        
        // Demonstrate Undo (Command Pattern)
        System.out.println("\n--- Undo Last Move ---");
        game.undoLastMove();
        
        System.out.println("\n--- Game Status: " + game.getStatus() + " ---");
    }
}

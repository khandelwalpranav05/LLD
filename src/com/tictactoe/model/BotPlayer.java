package com.tictactoe.model;

public class BotPlayer extends Player {
    public BotPlayer(String name, Symbol symbol) {
        super(name, symbol);
    }

    @Override
    public Cell decideMove(Board board) {
        // Simple Bot: Find first empty cell
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (board.getCells().get(i).get(j).isEmpty()) {
                    System.out.println("Bot " + name + " chose " + i + ", " + j);
                    return new Cell(i, j);
                }
            }
        }
        return null;
    }
}

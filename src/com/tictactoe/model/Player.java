package com.tictactoe.model;

public abstract class Player {
    protected String name;
    protected Symbol symbol;

    public Player(String name, Symbol symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() { return name; }
    public Symbol getSymbol() { return symbol; }

    // Abstract method to decide move (Human via input, Bot via algo)
    public abstract Cell decideMove(Board board);
}

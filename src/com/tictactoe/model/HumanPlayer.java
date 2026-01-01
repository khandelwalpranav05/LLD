package com.tictactoe.model;

import java.util.Scanner;

public class HumanPlayer extends Player {
    public HumanPlayer(String name, Symbol symbol) {
        super(name, symbol);
    }

    @Override
    public Cell decideMove(Board board) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(name + "'s turn (" + symbol + "). Enter row and col: ");
        int row = scanner.nextInt();
        int col = scanner.nextInt();
        return new Cell(row, col);
    }
}

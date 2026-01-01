package com.snakeladder.service;

import com.snakeladder.model.Board;
import com.snakeladder.model.Jump;
import com.snakeladder.model.Player;
import com.snakeladder.strategy.DiceStrategy;

import java.util.List;

public class Game {
    private Board board;
    private List<Player> players;
    private DiceStrategy diceStrategy;
    private int currentPlayerIndex;
    private boolean isGameEnded;

    public Game(Board board, List<Player> players, DiceStrategy diceStrategy) {
        this.board = board;
        this.players = players;
        this.diceStrategy = diceStrategy;
        this.currentPlayerIndex = 0;
        this.isGameEnded = false;
    }

    // Non-blocking method for Server Architecture
    public String makeMove(String playerId) {
        if (isGameEnded) return "Game Already Ended";
        
        Player currentPlayer = players.get(currentPlayerIndex);
        if (!currentPlayer.getName().equals(playerId)) {
            return "Not " + playerId + "'s turn. It is " + currentPlayer.getName() + "'s turn.";
        }

        // 1. Roll Dice
        int diceVal = diceStrategy.roll();
        int currentPos = currentPlayer.getPosition();
        int nextPos = currentPos + diceVal;
        
        StringBuilder log = new StringBuilder();
        log.append(currentPlayer.getName()).append(" rolled ").append(diceVal).append(". ");

        // 2. Check Bounds
        if (nextPos > board.getSize()) {
            log.append("Cannot move. Needs ").append(board.getSize() - currentPos);
        } else {
            // 3. Check Jumps
            Jump jump = board.getJump(nextPos);
            if (jump != null) {
                if (jump.getEnd() > jump.getStart()) {
                    log.append("Climbed Ladder! ");
                } else {
                    log.append("Bitten by Snake! ");
                }
                nextPos = jump.getEnd();
            }

            // 4. Update Position
            currentPlayer.setPosition(nextPos);
            log.append("Moved to ").append(nextPos);

            // 5. Check Win
            if (nextPos == board.getSize()) {
                log.append("\nWINNER: ").append(currentPlayer.getName()).append(" won the game!");
                isGameEnded = true;
                return log.toString();
            }
        }

        // 6. Next Turn
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        
        return log.toString();
    }
}

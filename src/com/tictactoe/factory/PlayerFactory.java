package com.tictactoe.factory;

import com.tictactoe.model.*;

/**
 * PlayerFactory - Creates Player instances.
 * Simplifies client logic and keeps Player creation centralized.
 */
public class PlayerFactory {
    
    // Hide constructor
    private PlayerFactory() {}

    public static Player createPlayer(PlayerType type, String name, Symbol symbol) {
        switch (type) {
            case HUMAN:
                return new HumanPlayer(name, symbol);
            
            case BOT:
                // Default to easy bot for now. 
                // In future can take DifficultyLevel as extra param.
                return new BotPlayer(name, symbol);
                
            default:
                throw new IllegalArgumentException("Unknown player type: " + type);
        }
    }
}

package com.snakeladder.strategy;

import java.util.Random;

public class NormalDiceStrategy implements DiceStrategy {
    private Random random = new Random();

    @Override
    public int roll() {
        return random.nextInt(6) + 1; // 1 to 6
    }
}

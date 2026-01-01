package com.atm.state;

import com.atm.ATM;
import com.atm.model.Card;

public class IdleState implements ATMState {
    @Override
    public void insertCard(ATM atm, Card card) {
        System.out.println("Card Inserted");
        atm.setCurrentCard(card);
        atm.setState(new HasCardState());
    }

    @Override
    public void enterPin(ATM atm, int pin) {
        System.out.println("Error: Insert card first");
    }

    @Override
    public void selectOperation(ATM atm, String operation) {
        System.out.println("Error: Insert card first");
    }

    @Override
    public void withdraw(ATM atm, int amount) {
        System.out.println("Error: Insert card first");
    }

    @Override
    public void exit(ATM atm) {
        System.out.println("Already Idle");
    }
}

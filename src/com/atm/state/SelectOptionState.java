package com.atm.state;

import com.atm.ATM;
import com.atm.model.Card;

public class SelectOptionState implements ATMState {
    @Override
    public void insertCard(ATM atm, Card card) {
        System.out.println("Error: Card already inserted");
    }

    @Override
    public void enterPin(ATM atm, int pin) {
        System.out.println("Error: Already authenticated");
    }

    @Override
    public void selectOperation(ATM atm, String operation) {
        if (operation.equalsIgnoreCase("WITHDRAW")) {
            System.out.println("Withdrawal Selected");
            atm.setState(new WithdrawalState());
        } else {
            System.out.println("Invalid Operation");
        }
    }

    @Override
    public void withdraw(ATM atm, int amount) {
        System.out.println("Error: Select operation first");
    }

    @Override
    public void exit(ATM atm) {
        System.out.println("Exiting...");
        atm.setCurrentCard(null); // Clear stale data
        atm.setState(new IdleState());
    }
}

package com.atm.state;

import com.atm.ATM;
import com.atm.model.Card;

public class HasCardState implements ATMState {
    @Override
    public void insertCard(ATM atm, Card card) {
        System.out.println("Error: Card already inserted");
    }

    @Override
    public void enterPin(ATM atm, int pin) {
        if (atm.getBankService().authenticateUser(atm.getCurrentCard().getCardNumber(), pin)) {
            System.out.println("PIN Correct");
            atm.setState(new SelectOptionState());
        } else {
            System.out.println("Invalid PIN. Ejecting Card.");
            atm.setState(new IdleState());
        }
    }

    @Override
    public void selectOperation(ATM atm, String operation) {
        System.out.println("Error: Enter PIN first");
    }

    @Override
    public void withdraw(ATM atm, int amount) {
        System.out.println("Error: Enter PIN first");
    }

    @Override
    public void exit(ATM atm) {
        System.out.println("Ejecting Card");
        atm.setCurrentCard(null); // Clear stale data
        atm.setState(new IdleState());
    }
}

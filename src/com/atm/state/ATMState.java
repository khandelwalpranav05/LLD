package com.atm.state;

import com.atm.ATM;
import com.atm.model.Card;

public interface ATMState {
    void insertCard(ATM atm, Card card);
    void enterPin(ATM atm, int pin);
    void selectOperation(ATM atm, String operation);
    void withdraw(ATM atm, int amount);
    void exit(ATM atm);
}

package com.atm;

import com.atm.chain.*;
import com.atm.model.Card;
import com.atm.service.BankService;
import com.atm.state.ATMState;
import com.atm.state.IdleState;

public class ATM {
    private static ATM instance;
    private ATMState currentState;
    private BankService bankService;
    private CashDispenser cashDispenser;
    private Card currentCard;

    private ATM() {
        this.currentState = new IdleState();
        this.bankService = new BankService();
        
        // Setup Chain: 2000 -> 500 -> 100
        this.cashDispenser = new TwoThousandDispenser(
            new FiveHundredDispenser(
                new OneHundredDispenser(null)
            )
        );
    }

    public static ATM getInstance() {
        if (instance == null) {
            instance = new ATM();
        }
        return instance;
    }

    public void setState(ATMState state) {
        this.currentState = state;
    }

    public ATMState getState() { return currentState; }
    public BankService getBankService() { return bankService; }
    public CashDispenser getCashDispenser() { return cashDispenser; }
    public Card getCurrentCard() { return currentCard; }
    public void setCurrentCard(Card card) { this.currentCard = card; }

    // Delegate methods to State
    public void insertCard(Card card) { currentState.insertCard(this, card); }
    public void enterPin(int pin) { currentState.enterPin(this, pin); }
    public void selectOperation(String operation) { currentState.selectOperation(this, operation); }
    public void withdraw(int amount) { currentState.withdraw(this, amount); }
    public void exit() { currentState.exit(this); }
}

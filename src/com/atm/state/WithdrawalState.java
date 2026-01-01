package com.atm.state;

import com.atm.ATM;
import com.atm.model.Account;
import com.atm.model.Card;

public class WithdrawalState implements ATMState {
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
        System.out.println("Error: Already in Withdrawal");
    }

    @Override
    public void withdraw(ATM atm, int amount) {
        Account account = atm.getBankService().getAccount(atm.getCurrentCard().getCardNumber());
        
        if (account.getBalance() < amount) {
            System.out.println("Insufficient Funds");
        } else {
            account.debit(amount);
            atm.getCashDispenser().dispense(amount);
            System.out.println("Please collect your cash");
        }
        
        // Return to Idle after transaction
        atm.setCurrentCard(null); // Clear stale data
        atm.setState(new IdleState());
    }

    @Override
    public void exit(ATM atm) {
        System.out.println("Cancelling Transaction");
        atm.setState(new IdleState());
    }
}

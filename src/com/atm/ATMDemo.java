package com.atm;

import com.atm.model.Account;
import com.atm.model.Card;

public class ATMDemo {
    public static void main(String[] args) {
        ATM atm = ATM.getInstance();
        
        // 1. Setup Bank Data
        Card myCard = new Card("1234567890", 1234);
        Account myAccount = new Account("1234567890", 10000.00);
        
        atm.getBankService().addCard(myCard);
        atm.getBankService().addAccount(myAccount);

        System.out.println("--- Scenario 1: Successful Withdrawal ---");
        atm.insertCard(myCard);
        atm.enterPin(1234);
        atm.selectOperation("WITHDRAW");
        atm.withdraw(2700); // Should be 1x2000, 1x500, 2x100

        System.out.println("\n--- Scenario 2: Invalid PIN ---");
        atm.insertCard(myCard);
        atm.enterPin(9999); // Wrong PIN
        
        System.out.println("\n--- Scenario 3: Insufficient Funds ---");
        atm.insertCard(myCard);
        atm.enterPin(1234);
        atm.selectOperation("WITHDRAW");
        atm.withdraw(20000); // More than balance
    }
}

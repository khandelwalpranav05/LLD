package com.atm.service;

import com.atm.model.Account;
import com.atm.model.Card;
import java.util.HashMap;
import java.util.Map;

public class BankService {
    private Map<String, Account> accounts = new HashMap<>();
    private Map<String, Card> cards = new HashMap<>(); // CardNum -> Card

    public void addAccount(Account account) {
        accounts.put(account.getAccountNumber(), account);
    }
    
    public void addCard(Card card) {
        cards.put(card.getCardNumber(), card);
    }

    public boolean authenticateUser(String cardNumber, int pin) {
        Card card = cards.get(cardNumber);
        return card != null && card.validatePin(pin);
    }

    public Account getAccount(String cardNumber) {
        // Mocking: In real life, map Card -> Account
        return accounts.get(cardNumber); 
    }
}

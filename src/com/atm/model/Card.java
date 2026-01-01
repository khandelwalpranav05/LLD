package com.atm.model;

public class Card {
    private String cardNumber;
    private int pin;

    public Card(String cardNumber, int pin) {
        this.cardNumber = cardNumber;
        this.pin = pin;
    }

    public String getCardNumber() { return cardNumber; }
    public boolean validatePin(int enteredPin) { return this.pin == enteredPin; }
}

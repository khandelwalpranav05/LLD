package com.atm.chain;

public abstract class CashDispenser {
    protected CashDispenser nextDispenser;

    public CashDispenser(CashDispenser nextDispenser) {
        this.nextDispenser = nextDispenser;
    }

    public void dispense(int amount) {
        if (nextDispenser != null) {
            nextDispenser.dispense(amount);
        } else if (amount > 0) {
            System.out.println("Error: Cannot dispense remaining " + amount);
        }
    }
}

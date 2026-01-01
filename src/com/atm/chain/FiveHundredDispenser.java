package com.atm.chain;

public class FiveHundredDispenser extends CashDispenser {
    public FiveHundredDispenser(CashDispenser nextDispenser) {
        super(nextDispenser);
    }

    @Override
    public void dispense(int amount) {
        if (amount >= 500) {
            int count = amount / 500;
            int remainder = amount % 500;
            System.out.println("Dispensing " + count + " x 500 Note");
            if (remainder > 0) super.dispense(remainder);
        } else {
            super.dispense(amount);
        }
    }
}

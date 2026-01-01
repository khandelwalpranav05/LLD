package com.atm.chain;

public class OneHundredDispenser extends CashDispenser {
    public OneHundredDispenser(CashDispenser nextDispenser) {
        super(nextDispenser);
    }

    @Override
    public void dispense(int amount) {
        if (amount >= 100) {
            int count = amount / 100;
            int remainder = amount % 100;
            System.out.println("Dispensing " + count + " x 100 Note");
            if (remainder > 0) super.dispense(remainder);
        } else {
            super.dispense(amount);
        }
    }
}

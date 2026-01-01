package com.atm.chain;

public class TwoThousandDispenser extends CashDispenser {
    public TwoThousandDispenser(CashDispenser nextDispenser) {
        super(nextDispenser);
    }

    @Override
    public void dispense(int amount) {
        if (amount >= 2000) {
            int count = amount / 2000;
            int remainder = amount % 2000;
            System.out.println("Dispensing " + count + " x 2000 Note");
            if (remainder > 0) super.dispense(remainder);
        } else {
            super.dispense(amount);
        }
    }
}

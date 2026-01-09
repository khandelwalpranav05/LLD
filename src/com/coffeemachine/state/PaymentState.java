package com.coffeemachine.state;

import com.coffeemachine.service.CoffeeMachine;
import com.coffeemachine.model.Beverage;

public class PaymentState implements MachineState {
    private CoffeeMachine machine;

    public PaymentState(CoffeeMachine machine) {
        this.machine = machine;
    }

    @Override
    public void selectBeverage(Beverage beverage) {
        System.out.println("Already selected " + machine.getCurrentBeverage().getDescription());
    }

    @Override
    public void insertMoney(double amount) {
        machine.addBalance(amount);
        double balance = machine.getCurrentBalance();
        double cost = machine.getCurrentBeverage().getCost();
        
        System.out.println("Inserted: $" + amount + ". Total: $" + balance);
        
        if (balance >= cost) {
            // Enough money - proceed to dispense
            machine.setState(machine.getDispensingState());
            machine.dispense();
        } else {
            // Not enough - tell user how much more is needed
            double remaining = cost - balance;
            System.out.println("Please insert $" + String.format("%.2f", remaining) + " more.");
        }
    }

    @Override
    public void dispense() {
        System.out.println("Please insert full amount.");
    }
}

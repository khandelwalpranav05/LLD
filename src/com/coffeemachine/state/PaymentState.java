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
        System.out.println("Inserted: $" + amount + ". Total: $" + machine.getCurrentBalance());
        
        if (machine.getCurrentBalance() >= machine.getCurrentBeverage().getCost()) {
            machine.setState(machine.getDispensingState());
            machine.dispense(); // Auto-trigger dispense
        }
    }

    @Override
    public void dispense() {
        System.out.println("Please insert full amount.");
    }
}

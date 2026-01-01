package com.coffeemachine.state;

import com.coffeemachine.service.CoffeeMachine;
import com.coffeemachine.model.Beverage;

public class IdleState implements MachineState {
    private CoffeeMachine machine;

    public IdleState(CoffeeMachine machine) {
        this.machine = machine;
    }

    @Override
    public void selectBeverage(Beverage beverage) {
        System.out.println("Selected: " + beverage.getDescription() + " ($" + beverage.getCost() + ")");
        
        // Check Inventory BEFORE proceeding
        java.util.List<String> required = beverage.getIngredients();
        if (!machine.getInventory().checkIngredients(required.toArray(new String[0]))) {
            System.out.println("Error: Out of Stock for " + beverage.getDescription());
            return; // Stay in Idle State
        }

        machine.setCurrentBeverage(beverage);
        machine.setState(machine.getPaymentState());
    }

    @Override
    public void insertMoney(double amount) {
        System.out.println("Please select a beverage first.");
    }

    @Override
    public void dispense() {
        System.out.println("Please select a beverage first.");
    }
}

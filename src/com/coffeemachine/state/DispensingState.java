package com.coffeemachine.state;

import com.coffeemachine.service.CoffeeMachine;

public class DispensingState implements MachineState {
    private CoffeeMachine machine;

    public DispensingState(CoffeeMachine machine) {
        this.machine = machine;
    }

    @Override
    public void selectBeverage(com.coffeemachine.model.Beverage beverage) {
        System.out.println("Please wait, dispensing...");
    }

    @Override
    public void insertMoney(double amount) {
        System.out.println("Please wait, dispensing...");
    }

    @Override
    public void dispense() {
        System.out.println("Dispensing " + machine.getCurrentBeverage().getDescription());
        
        // Consume ingredients dynamically
        java.util.List<String> required = machine.getCurrentBeverage().getIngredients();
        machine.getInventory().consume(required.toArray(new String[0]));
        
        double change = machine.getCurrentBalance() - machine.getCurrentBeverage().getCost();
        if (change > 0) {
            System.out.println("Returning change: $" + change);
        }
        
        machine.resetBalance();
        machine.setCurrentBeverage(null);
        machine.setState(machine.getIdleState());
    }
}

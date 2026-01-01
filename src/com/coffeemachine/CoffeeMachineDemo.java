package com.coffeemachine;

import com.coffeemachine.service.CoffeeMachine;
import com.coffeemachine.model.*;
import com.coffeemachine.decorator.*;

public class CoffeeMachineDemo {
    public static void main(String[] args) {
        System.out.println("Initializing Coffee Machine...");
        CoffeeMachine machine = CoffeeMachine.getInstance();

        // 1. User selects Espresso
        System.out.println("\n--- Scenario 1: Espresso ---");
        Beverage espresso = new Espresso();
        machine.selectBeverage(espresso);
        machine.insertMoney(2.00);

        // 2. User selects Latte (Espresso + Milk + Sugar)
        System.out.println("\n--- Scenario 2: Custom Latte ---");
        Beverage latte = new Espresso();
        latte = new Milk(latte);
        latte = new Sugar(latte);
        
        machine.selectBeverage(latte);
        machine.insertMoney(1.00); // Not enough
        machine.insertMoney(2.00); // Total 3.00, Cost 2.75
        
        // 3. Out of Stock Scenario
        System.out.println("\n--- Scenario 3: Out of Stock ---");
        // Force empty Milk
        machine.getInventory().refill("Milk", -1000); // Hack to empty it
        
        Beverage milky = new Milk(new Espresso());
        machine.selectBeverage(milky); // Should fail
    }
}

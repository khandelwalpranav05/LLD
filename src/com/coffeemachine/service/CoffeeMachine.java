package com.coffeemachine.service;

import com.coffeemachine.state.*;
import com.coffeemachine.model.Beverage;

/**
 * Singleton Context Class for State Pattern.
 */
public class CoffeeMachine {
    private static CoffeeMachine instance;
    private MachineState currentState;
    private Inventory inventory;
    private Beverage currentBeverage;
    private double currentBalance;

    // States
    private MachineState idleState;
    private MachineState paymentState;
    private MachineState dispensingState;

    private CoffeeMachine() {
        inventory = new Inventory();
        idleState = new IdleState(this);
        paymentState = new PaymentState(this);
        dispensingState = new DispensingState(this);
        currentState = idleState;
    }

    public static synchronized CoffeeMachine getInstance() {
        if (instance == null) {
            instance = new CoffeeMachine();
        }
        return instance;
    }

    public void setState(MachineState state) {
        this.currentState = state;
    }

    public MachineState getIdleState() { return idleState; }
    public MachineState getPaymentState() { return paymentState; }
    public MachineState getDispensingState() { return dispensingState; }
    
    public Inventory getInventory() { return inventory; }

    // Actions delegated to current state
    public void selectBeverage(Beverage beverage) {
        currentState.selectBeverage(beverage);
    }

    public void insertMoney(double amount) {
        currentState.insertMoney(amount);
    }
    
    public void dispense() {
        currentState.dispense();
    }

    // Getters/Setters for State transitions
    public void setCurrentBeverage(Beverage beverage) { this.currentBeverage = beverage; }
    public Beverage getCurrentBeverage() { return currentBeverage; }
    public void addBalance(double amount) { this.currentBalance += amount; }
    public double getCurrentBalance() { return currentBalance; }
    public void resetBalance() { this.currentBalance = 0; }
}

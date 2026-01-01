package com.coffeemachine.state;

import com.coffeemachine.model.Beverage;

/**
 * State Interface for State Pattern.
 * Defines actions available on the machine.
 */
public interface MachineState {
    void selectBeverage(Beverage beverage);
    void insertMoney(double amount);
    void dispense();
}

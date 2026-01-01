package com.coffeemachine.decorator;

import com.coffeemachine.model.Beverage;

/**
 * Decorator Abstract Class.
 * It implements Beverage AND holds a Beverage.
 * This allows us to wrap a drink inside a condiment.
 */
public abstract class CondimentDecorator implements Beverage {
    protected Beverage beverage;

    public CondimentDecorator(Beverage beverage) {
        this.beverage = beverage;
    }

    public abstract String getDescription();
    public abstract java.util.List<String> getIngredients();
}

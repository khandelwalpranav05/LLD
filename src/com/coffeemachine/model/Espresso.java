package com.coffeemachine.model;

public class Espresso implements Beverage {
    @Override
    public String getDescription() {
        return "Espresso";
    }

    @Override
    public double getCost() {
        return 2.00;
    }

    @Override
    public java.util.List<String> getIngredients() {
        return java.util.Arrays.asList("CoffeeBeans", "Water");
    }
}

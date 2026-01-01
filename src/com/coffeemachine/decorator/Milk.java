package com.coffeemachine.decorator;

import com.coffeemachine.model.Beverage;

public class Milk extends CondimentDecorator {
    public Milk(Beverage beverage) {
        super(beverage);
    }

    @Override
    public String getDescription() {
        return beverage.getDescription() + ", Milk";
    }

    @Override
    public double getCost() {
        return beverage.getCost() + 0.50;
    }

    @Override
    public java.util.List<String> getIngredients() {
        java.util.List<String> ingredients = new java.util.ArrayList<>(beverage.getIngredients());
        ingredients.add("Milk");
        return ingredients;
    }
}

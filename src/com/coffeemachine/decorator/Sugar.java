package com.coffeemachine.decorator;

import com.coffeemachine.model.Beverage;

public class Sugar extends CondimentDecorator {
    public Sugar(Beverage beverage) {
        super(beverage);
    }

    @Override
    public String getDescription() {
        return beverage.getDescription() + ", Sugar";
    }

    @Override
    public double getCost() {
        return beverage.getCost() + 0.25;
    }

    @Override
    public java.util.List<String> getIngredients() {
        java.util.List<String> ingredients = new java.util.ArrayList<>(beverage.getIngredients());
        ingredients.add("Sugar");
        return ingredients;
    }
}

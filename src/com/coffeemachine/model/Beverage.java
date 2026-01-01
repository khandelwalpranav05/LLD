package com.coffeemachine.model;

/**
 * Component Interface for Decorator Pattern.
 * Represents any drink (Base or Decorated).
 */
public interface Beverage {
    String getDescription();
    double getCost();
    java.util.List<String> getIngredients();
}

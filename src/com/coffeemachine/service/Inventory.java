package com.coffeemachine.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Inventory {
    private Map<String, Integer> ingredients;

    public Inventory() {
        ingredients = new ConcurrentHashMap<>();
        ingredients.put("CoffeeBeans", 100);
        ingredients.put("Milk", 100);
        ingredients.put("Sugar", 100);
        ingredients.put("Water", 100);
    }

    public boolean checkIngredients(String... required) {
        for (String item : required) {
            if (ingredients.getOrDefault(item, 0) <= 0) {
                return false;
            }
        }
        return true;
    }

    public void consume(String... required) {
        for (String item : required) {
            ingredients.put(item, ingredients.get(item) - 1);
        }
    }
    
    public void refill(String item, int amount) {
        ingredients.put(item, ingredients.getOrDefault(item, 0) + amount);
    }
}

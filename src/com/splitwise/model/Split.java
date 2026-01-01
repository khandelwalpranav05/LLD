package com.splitwise.model;

/**
 * Split represents how much ONE user owes for an expense.
 * This is pure DATA - no logic here.
 * The SplitStrategy calculates these values.
 */
public class Split {
    private User user;
    private double amount;
    private double percentage; // Only used for percentage splits
    
    public Split(User user, double amount) {
        this.user = user;
        this.amount = amount;
    }
    
    public Split(User user, double amount, double percentage) {
        this.user = user;
        this.amount = amount;
        this.percentage = percentage;
    }
    
    public User getUser() { return user; }
    public double getAmount() { return amount; }
    public double getPercentage() { return percentage; }
    
    @Override
    public String toString() {
        return user.getName() + " owes $" + String.format("%.2f", amount);
    }
}

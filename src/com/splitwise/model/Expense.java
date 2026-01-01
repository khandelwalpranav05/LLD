package com.splitwise.model;

import java.util.List;
import java.util.UUID;

/**
 * Expense represents a payment made by one user that needs to be split.
 * 
 * Example: Alice pays $100 for dinner.
 * - paidBy = Alice
 * - amount = 100
 * - splits = [Alice owes $33.33, Bob owes $33.33, Carol owes $33.33]
 */
public class Expense {
    private String id;
    private double amount;
    private String description;
    private User paidBy;
    private List<Split> splits;
    private SplitType splitType;
    
    public Expense(double amount, String description, User paidBy, 
                   List<Split> splits, SplitType splitType) {
        this.id = UUID.randomUUID().toString();
        this.amount = amount;
        this.description = description;
        this.paidBy = paidBy;
        this.splits = splits;
        this.splitType = splitType;
    }
    
    public String getId() { return id; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public User getPaidBy() { return paidBy; }
    public List<Split> getSplits() { return splits; }
    public SplitType getSplitType() { return splitType; }
    
    @Override
    public String toString() {
        return paidBy.getName() + " paid $" + String.format("%.2f", amount) + " for " + description;
    }
}

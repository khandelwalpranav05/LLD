package com.splitwise.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction represents a record of money movement.
 * Used for audit trail and history.
 * 
 * Two types:
 * - EXPENSE: When calculating splits (not actual money transfer)
 * - SETTLEMENT: When someone pays another person back
 */
public class Transaction {
    private String id;
    private User from;
    private User to;
    private double amount;
    private TransactionType type;
    private LocalDateTime timestamp;
    private String description;
    
    public Transaction(User from, User to, double amount, 
                       TransactionType type, String description) {
        this.id = UUID.randomUUID().toString();
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getId() { return id; }
    public User getFrom() { return from; }
    public User getTo() { return to; }
    public double getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s -> %s: $%.2f (%s)", 
            type, from.getName(), to.getName(), amount, description);
    }
}

package com.splitwise.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * User represents a person in the Splitwise system.
 * 
 * KEY DESIGN: Each user maintains a balance map:
 * - Key: userId of another user
 * - Value: how much that user owes ME
 *   - Positive = they owe me
 *   - Negative = I owe them
 */
public class User {
    private String id;
    private String name;
    private String email;
    
    // Thread-safe map for concurrent access
    private Map<String, Double> balances;
    
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.balances = new ConcurrentHashMap<>();
    }
    
    /**
     * Get how much another user owes me.
     * Positive = they owe me, Negative = I owe them
     */
    public double getBalance(String userId) {
        return balances.getOrDefault(userId, 0.0);
    }
    
    /**
     * Update the balance with another user.
     * This is synchronized to prevent race conditions.
     */
    public synchronized void updateBalance(String userId, double amount) {
        double current = balances.getOrDefault(userId, 0.0);
        double newBalance = current + amount;
        
        // Clean up zero balances
        if (Math.abs(newBalance) < 0.01) {
            balances.remove(userId);
        } else {
            balances.put(userId, newBalance);
        }
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Map<String, Double> getBalances() { return balances; }
    
    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}

package com.splitwise.strategy;

import com.splitwise.model.Split;
import com.splitwise.model.User;
import java.util.List;
import java.util.Map;

/**
 * SplitStrategy defines how to calculate splits.
 * This is the STRATEGY PATTERN - each split type is a different strategy.
 * 
 * Benefits:
 * 1. Adding new split types = add new class, no changes to existing code
 * 2. Each strategy can be tested in isolation
 * 3. Clean separation of concerns
 */
public interface SplitStrategy {
    
    /**
     * Calculate splits for the given expense.
     * 
     * @param amount Total expense amount
     * @param participants Map of userId -> their contribution (interpretation varies by strategy)
     * @param users Map of userId -> User object
     * @return List of Split objects
     */
    List<Split> split(double amount, Map<String, Double> participants, Map<String, User> users);
    
    /**
     * Validate the input before splitting.
     * E.g., percentages must add up to 100%
     */
    boolean validate(double amount, Map<String, Double> participants);
}

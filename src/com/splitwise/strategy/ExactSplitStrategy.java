package com.splitwise.strategy;

import com.splitwise.model.Split;
import com.splitwise.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ExactSplitStrategy splits based on exact amounts specified.
 * 
 * Example: $100 total, Alice pays $50, Bob pays $30, Carol pays $20
 * The map values are the exact amounts.
 */
public class ExactSplitStrategy implements SplitStrategy {
    
    @Override
    public List<Split> split(double amount, Map<String, Double> participants, Map<String, User> users) {
        List<Split> splits = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : participants.entrySet()) {
            User user = users.get(entry.getKey());
            double exactAmount = entry.getValue();
            splits.add(new Split(user, exactAmount));
        }
        
        return splits;
    }
    
    @Override
    public boolean validate(double amount, Map<String, Double> participants) {
        // Exact amounts must sum to the total
        double sum = participants.values().stream().mapToDouble(Double::doubleValue).sum();
        return Math.abs(sum - amount) < 0.01; // Allow tiny floating point error
    }
}

package com.splitwise.strategy;

import com.splitwise.model.Split;
import com.splitwise.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PercentageSplitStrategy splits based on percentages.
 * 
 * Example: $100 total, Alice 50%, Bob 30%, Carol 20%
 * The map values are the percentages (must sum to 100).
 */
public class PercentageSplitStrategy implements SplitStrategy {
    
    @Override
    public List<Split> split(double amount, Map<String, Double> participants, Map<String, User> users) {
        List<Split> splits = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : participants.entrySet()) {
            User user = users.get(entry.getKey());
            double percentage = entry.getValue();
            double share = (percentage / 100.0) * amount;
            splits.add(new Split(user, share, percentage));
        }
        
        return splits;
    }
    
    @Override
    public boolean validate(double amount, Map<String, Double> participants) {
        // Percentages must sum to 100
        double sum = participants.values().stream().mapToDouble(Double::doubleValue).sum();
        return Math.abs(sum - 100.0) < 0.01;
    }
}

package com.splitwise.strategy;

import com.splitwise.model.Split;
import com.splitwise.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EqualSplitStrategy splits the expense equally among all participants.
 * 
 * Example: $100 among 3 people = $33.33 each
 */
public class EqualSplitStrategy implements SplitStrategy {
    
    @Override
    public List<Split> split(double amount, Map<String, Double> participants, Map<String, User> users) {
        List<Split> splits = new ArrayList<>();
        int count = participants.size();
        double perPerson = amount / count;
        
        for (String userId : participants.keySet()) {
            User user = users.get(userId);
            splits.add(new Split(user, perPerson));
        }
        
        return splits;
    }
    
    @Override
    public boolean validate(double amount, Map<String, Double> participants) {
        // Equal split is always valid if there's at least one participant
        return !participants.isEmpty() && amount > 0;
    }
}

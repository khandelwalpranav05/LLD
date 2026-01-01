package com.splitwise.service;

import com.splitwise.model.*;
import com.splitwise.strategy.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SplitwiseService is the main service class (Singleton).
 * 
 * WHY SINGLETON?
 * - One central place to manage all users, groups, and balances
 * - Ensures data consistency across the application
 * - In production, this would be a stateless service with data in a database
 */
public class SplitwiseService {
    private static SplitwiseService instance;
    
    // Thread-safe collections for concurrent access
    private Map<String, User> users;
    private Map<String, Group> groups;
    private List<Transaction> transactions;
    
    // Strategy instances (reusable, stateless)
    private Map<SplitType, SplitStrategy> strategies;
    
    private SplitwiseService() {
        this.users = new ConcurrentHashMap<>();
        this.groups = new ConcurrentHashMap<>();
        this.transactions = new CopyOnWriteArrayList<>();
        
        // Initialize strategies (Factory Pattern)
        this.strategies = new HashMap<>();
        strategies.put(SplitType.EQUAL, new EqualSplitStrategy());
        strategies.put(SplitType.EXACT, new ExactSplitStrategy());
        strategies.put(SplitType.PERCENTAGE, new PercentageSplitStrategy());
    }
    
    public static synchronized SplitwiseService getInstance() {
        if (instance == null) {
            instance = new SplitwiseService();
        }
        return instance;
    }
    
    // ============ USER MANAGEMENT ============
    
    public void addUser(User user) {
        users.put(user.getId(), user);
        System.out.println("Added user: " + user);
    }
    
    public User getUser(String userId) {
        return users.get(userId);
    }
    
    // ============ GROUP MANAGEMENT ============
    
    public Group createGroup(String name, List<User> members) {
        Group group = new Group(name);
        for (User user : members) {
            group.addMember(user);
        }
        groups.put(group.getId(), group);
        System.out.println("Created group: " + group);
        return group;
    }
    
    public Group getGroup(String groupId) {
        return groups.get(groupId);
    }
    
    // ============ EXPENSE MANAGEMENT ============
    
    /**
     * Add an expense and update balances.
     * 
     * @param groupId Group where expense was made
     * @param paidBy User who paid
     * @param amount Total amount
     * @param description What was it for
     * @param splitType How to split (EQUAL, EXACT, PERCENTAGE)
     * @param participantShares Map of userId -> share value (interpretation depends on splitType)
     */
    public synchronized Expense addExpense(String groupId, User paidBy, double amount,
                                           String description, SplitType splitType,
                                           Map<String, Double> participantShares) {
        // 1. Get the strategy
        SplitStrategy strategy = strategies.get(splitType);
        
        // 2. Validate
        if (!strategy.validate(amount, participantShares)) {
            throw new IllegalArgumentException("Invalid split: amounts don't match for " + splitType);
        }
        
        // 3. Calculate splits
        List<Split> splits = strategy.split(amount, participantShares, users);
        
        // 4. Create expense
        Expense expense = new Expense(amount, description, paidBy, splits, splitType);
        
        // 5. Update balances
        updateBalances(paidBy, splits);
        
        // 6. Add to group (if groupId is provided)
        if (groupId != null) {
            Group group = groups.get(groupId);
            if (group != null) {
                group.addExpense(expense);
            }
        }
        
        System.out.println("Added expense: " + expense);
        return expense;
    }
    
    /**
     * Add a PERSONAL expense between users (no group required).
     * 
     * Example: Alice and Bob split a $50 coffee, not in any group.
     */
    public synchronized Expense addPersonalExpense(User paidBy, double amount,
                                                   String description, SplitType splitType,
                                                   Map<String, Double> participantShares) {
        // Call the main method with null groupId
        return addExpense(null, paidBy, amount, description, splitType, participantShares);
    }
    
    /**
     * Update balances after an expense.
     * 
     * Logic:
     * - For each split, the debtor owes the payer
     * - We update both sides of the balance
     */
    private void updateBalances(User payer, List<Split> splits) {
        for (Split split : splits) {
            User debtor = split.getUser();
            double amount = split.getAmount();
            
            // Skip if the debtor is the payer (they don't owe themselves)
            if (debtor.getId().equals(payer.getId())) {
                continue;
            }
            
            // Payer is owed money by debtor
            payer.updateBalance(debtor.getId(), amount);
            
            // Debtor owes money to payer (negative from their perspective)
            debtor.updateBalance(payer.getId(), -amount);
        }
    }
    
    // ============ SETTLEMENT ============
    
    /**
     * Settle up: One user pays another to clear their debt.
     */
    public synchronized Transaction settleUp(User from, User to, double amount) {
        // Create transaction record
        Transaction transaction = new Transaction(from, to, amount, 
            TransactionType.SETTLEMENT, "Settlement");
        transactions.add(transaction);
        
        // Update balances (reverse of expense)
        // 'from' paid 'to', so 'from' now owes less to 'to'
        from.updateBalance(to.getId(), amount);  // from is owed more by to
        to.updateBalance(from.getId(), -amount); // to owes less to from
        
        System.out.println("Settlement: " + transaction);
        return transaction;
    }
    
    // ============ REPORTING ============
    
    /**
     * Get all balances for a user.
     */
    public Map<String, Double> getBalances(String userId) {
        User user = users.get(userId);
        if (user == null) return Collections.emptyMap();
        return user.getBalances();
    }
    
    /**
     * Get transaction history for a user.
     */
    public List<Transaction> getTransactionHistory(String userId) {
        List<Transaction> history = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getFrom().getId().equals(userId) || t.getTo().getId().equals(userId)) {
                history.add(t);
            }
        }
        return history;
    }
    
    /**
     * Print all balances for a user.
     */
    public void printBalances(String userId) {
        User user = users.get(userId);
        if (user == null) {
            System.out.println("User not found: " + userId);
            return;
        }
        
        System.out.println("\n=== Balances for " + user.getName() + " ===");
        Map<String, Double> balances = user.getBalances();
        
        if (balances.isEmpty()) {
            System.out.println("All settled up!");
            return;
        }
        
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            User other = users.get(entry.getKey());
            double amount = entry.getValue();
            
            if (amount > 0) {
                System.out.println(other.getName() + " owes you $" + String.format("%.2f", amount));
            } else {
                System.out.println("You owe " + other.getName() + " $" + String.format("%.2f", -amount));
            }
        }
    }
}

package com.splitwise.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Group represents a collection of users who share expenses.
 * Uses CopyOnWriteArrayList for thread-safe iteration.
 */
public class Group {
    private String id;
    private String name;
    private List<User> members;
    private List<Expense> expenses;
    
    public Group(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.members = new CopyOnWriteArrayList<>();
        this.expenses = new CopyOnWriteArrayList<>();
    }
    
    public void addMember(User user) {
        if (!members.contains(user)) {
            members.add(user);
        }
    }
    
    public void removeMember(User user) {
        members.remove(user);
    }
    
    public void addExpense(Expense expense) {
        expenses.add(expense);
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public List<User> getMembers() { return members; }
    public List<Expense> getExpenses() { return expenses; }
    
    @Override
    public String toString() {
        return name + " (" + members.size() + " members)";
    }
}

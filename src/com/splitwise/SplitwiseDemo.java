package com.splitwise;

import com.splitwise.model.*;
import com.splitwise.service.SplitwiseService;
import java.util.*;

/**
 * SplitwiseDemo demonstrates all features of the Splitwise system.
 */
public class SplitwiseDemo {
    public static void main(String[] args) {
        SplitwiseService service = SplitwiseService.getInstance();
        
        // ============ 1. CREATE USERS ============
        System.out.println("=== Creating Users ===");
        User alice = new User("u1", "Alice", "alice@email.com");
        User bob = new User("u2", "Bob", "bob@email.com");
        User carol = new User("u3", "Carol", "carol@email.com");
        
        service.addUser(alice);
        service.addUser(bob);
        service.addUser(carol);
        
        // ============ 2. CREATE GROUP ============
        System.out.println("\n=== Creating Group ===");
        Group trip = service.createGroup("Beach Trip", Arrays.asList(alice, bob, carol));
        
        // ============ 3. ADD EXPENSE - EQUAL SPLIT ============
        System.out.println("\n=== Adding Expense (Equal Split) ===");
        // Alice pays $120 for dinner, split equally among all 3
        Map<String, Double> equalSplit = new HashMap<>();
        equalSplit.put("u1", 0.0); // Value ignored for equal split
        equalSplit.put("u2", 0.0);
        equalSplit.put("u3", 0.0);
        
        service.addExpense(trip.getId(), alice, 120.0, "Dinner", SplitType.EQUAL, equalSplit);
        
        // Print balances after equal split
        service.printBalances("u1"); // Alice
        service.printBalances("u2"); // Bob
        
        // ============ 4. ADD EXPENSE - EXACT SPLIT ============
        System.out.println("\n=== Adding Expense (Exact Split) ===");
        // Bob pays $100 for activities: Alice $50, Bob $30, Carol $20
        Map<String, Double> exactSplit = new HashMap<>();
        exactSplit.put("u1", 50.0);
        exactSplit.put("u2", 30.0);
        exactSplit.put("u3", 20.0);
        
        service.addExpense(trip.getId(), bob, 100.0, "Water Sports", SplitType.EXACT, exactSplit);
        
        service.printBalances("u1");
        service.printBalances("u2");
        
        // ============ 5. ADD EXPENSE - PERCENTAGE SPLIT ============
        System.out.println("\n=== Adding Expense (Percentage Split) ===");
        // Carol pays $200 for hotel: Alice 50%, Bob 30%, Carol 20%
        Map<String, Double> percentSplit = new HashMap<>();
        percentSplit.put("u1", 50.0); // 50%
        percentSplit.put("u2", 30.0); // 30%
        percentSplit.put("u3", 20.0); // 20%
        
        service.addExpense(trip.getId(), carol, 200.0, "Hotel", SplitType.PERCENTAGE, percentSplit);
        
        // ============ 6. FINAL BALANCES ============
        System.out.println("\n=== Final Balances ===");
        service.printBalances("u1");
        service.printBalances("u2");
        service.printBalances("u3");
        
        // ============ 7. SETTLE UP ============
        System.out.println("\n=== Settlement ===");
        // Bob settles $40 with Alice
        service.settleUp(bob, alice, 40.0);
        
        System.out.println("\n=== Balances After Settlement ===");
        service.printBalances("u1");
        service.printBalances("u2");
        
        // ============ 8. PARTIAL SPLIT (5 users, only 2 in expense) ============
        System.out.println("\n=== Adding 2 More Users ===");
        User dave = new User("u4", "Dave", "dave@email.com");
        User eve = new User("u5", "Eve", "eve@email.com");
        service.addUser(dave);
        service.addUser(eve);
        trip.addMember(dave);
        trip.addMember(eve);
        System.out.println("Group now has " + trip.getMembers().size() + " members");
        
        System.out.println("\n=== Adding Expense (Only Dave and Eve) ===");
        // Dave pays $50 for coffee, split only with Eve
        Map<String, Double> partialSplit = new HashMap<>();
        partialSplit.put("u4", 0.0);  // Dave
        partialSplit.put("u5", 0.0);  // Eve
        // Alice, Bob, Carol are NOT in this expense
        
        service.addExpense(trip.getId(), dave, 50.0, "Coffee", SplitType.EQUAL, partialSplit);
        
        System.out.println("\n=== Balances After Partial Split ===");
        service.printBalances("u1"); // Alice - should NOT be affected
        service.printBalances("u4"); // Dave - should show Eve owes him
        service.printBalances("u5"); // Eve - should show she owes Dave
        
        // ============ 9. PERSONAL EXPENSE (No Group) ============
        System.out.println("\n=== Adding Personal Expense (No Group) ===");
        // Alice and Bob split $80 coffee, NOT in any group
        Map<String, Double> personalSplit = new HashMap<>();
        personalSplit.put("u1", 0.0);  // Alice
        personalSplit.put("u2", 0.0);  // Bob
        
        // Use the NEW personal expense method
        service.addPersonalExpense(alice, 80.0, "Coffee (just us)", SplitType.EQUAL, personalSplit);
        
        System.out.println("\n=== Balances After Personal Expense ===");
        service.printBalances("u1");
        service.printBalances("u2");
    }
}

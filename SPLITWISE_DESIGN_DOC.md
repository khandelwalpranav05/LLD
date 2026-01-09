# Splitwise - Low Level Design Document

## 📋 Table of Contents
1. [Problem Statement](#problem-statement)
2. [Requirements Analysis](#requirements-analysis)
3. [System Architecture](#system-architecture)
4. [Class Design & Responsibilities](#class-design--responsibilities)
5. [Design Patterns Used](#design-patterns-used)
6. [Core Algorithm: Balance Management](#core-algorithm-balance-management)
7. [Concurrency & Thread Safety](#concurrency--thread-safety)
8. [Key Design Decisions & Trade-offs](#key-design-decisions--trade-offs)
9. [Edge Cases & Failure Handling](#edge-cases--failure-handling)
10. [Extensibility & Future Enhancements](#extensibility--future-enhancements)
11. [Interview Discussion Points](#interview-discussion-points)
12. [Quick Reference Cheat Sheet](#quick-reference-cheat-sheet)

---

## Problem Statement

Design an expense-sharing application (like Splitwise) that:
- Allows users to add expenses and split them among participants
- Supports multiple split types (equal, exact amounts, percentages)
- Tracks who owes whom and how much
- Enables settlements between users
- Works within groups or between individual users

---

## Requirements Analysis

### Functional Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| FR1 | Add users to the system | Must Have |
| FR2 | Create groups with multiple members | Must Have |
| FR3 | Add expenses with different split types | Must Have |
| FR4 | Track balances between users | Must Have |
| FR5 | Settle up debts between users | Must Have |
| FR6 | Support personal expenses (no group) | Should Have |
| FR7 | View transaction history | Should Have |
| FR8 | Partial splits (subset of group members) | Should Have |

### Non-Functional Requirements

| ID | Requirement | How We Address It |
|----|-------------|-------------------|
| NFR1 | **Concurrency** | ConcurrentHashMap, synchronized methods |
| NFR2 | **Extensibility** | Strategy pattern for split types |
| NFR3 | **Accuracy** | Handle floating-point precision issues |
| NFR4 | **Consistency** | Atomic balance updates |
| NFR5 | **Auditability** | Transaction history logging |

---

## System Architecture

### High-Level Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      SPLITWISE SYSTEM                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│                    ┌─────────────────────┐                      │
│                    │  SplitwiseService   │                      │
│                    │    (Singleton)      │                      │
│                    │                     │                      │
│                    │  - addExpense()     │                      │
│                    │  - settleUp()       │                      │
│                    │  - getBalances()    │                      │
│                    └──────────┬──────────┘                      │
│                               │                                  │
│          ┌────────────────────┼────────────────────┐            │
│          │                    │                    │            │
│          ▼                    ▼                    ▼            │
│   ┌────────────┐      ┌────────────┐      ┌────────────────┐   │
│   │   User     │      │   Group    │      │ SplitStrategy  │   │
│   │            │      │            │      │  (Interface)   │   │
│   │ - balances │      │ - members  │      │                │   │
│   │ - update() │      │ - expenses │      │ ┌────────────┐ │   │
│   └────────────┘      └────────────┘      │ │   Equal    │ │   │
│          │                    │           │ │   Exact    │ │   │
│          │                    │           │ │ Percentage │ │   │
│          │                    │           │ └────────────┘ │   │
│          │                    │           └────────────────┘   │
│          │                    │                                  │
│          ▼                    ▼                                  │
│   ┌────────────┐      ┌────────────┐                           │
│   │Transaction │      │  Expense   │                           │
│   │  (Audit)   │      │  - splits  │                           │
│   └────────────┘      └────────────┘                           │
│                               │                                  │
│                               ▼                                  │
│                        ┌────────────┐                           │
│                        │   Split    │                           │
│                        │ (per user) │                           │
│                        └────────────┘                           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.splitwise/
├── model/
│   ├── User.java          # User with balance map
│   ├── Group.java         # Collection of users
│   ├── Expense.java       # A payment to be split
│   ├── Split.java         # One user's share of expense
│   ├── SplitType.java     # EQUAL, EXACT, PERCENTAGE
│   ├── Transaction.java   # Audit record
│   └── TransactionType.java # EXPENSE, SETTLEMENT
│
├── service/
│   └── SplitwiseService.java  # Main service (Singleton)
│
├── strategy/
│   ├── SplitStrategy.java     # Interface
│   ├── EqualSplitStrategy.java
│   ├── ExactSplitStrategy.java
│   └── PercentageSplitStrategy.java
│
└── SplitwiseDemo.java
```

---

## Class Design & Responsibilities

### 1. User - The Balance Holder

```java
public class User {
    private String id;
    private String name;
    private String email;
    
    // KEY: Balance map - who owes me how much
    private Map<String, Double> balances;  // ConcurrentHashMap
    
    // Positive = they owe me
    // Negative = I owe them
    public synchronized void updateBalance(String userId, double amount);
}
```

**Why store balances in User?**

| Approach | Pros | Cons |
|----------|------|------|
| **Balance in User** (Our choice) | O(1) lookup, simple query | Denormalized, needs sync |
| Separate Balance table | Normalized, single source | O(n) to get all balances |
| Calculate from expenses | Always accurate | O(all_expenses) - very slow |

### 2. Expense - The Split Container

```java
public class Expense {
    private String id;
    private double amount;
    private String description;
    private User paidBy;           // Who paid
    private List<Split> splits;    // Who owes what
    private SplitType splitType;   // How it was split
}
```

### 3. Split - Individual Share

```java
public class Split {
    private User user;        // Who owes
    private double amount;    // How much
    private double percentage; // Only for percentage splits
}
```

**Design Decision: Split is pure data, no logic.**
- Strategy calculates the Split values
- Expense holds the Splits
- Service updates balances based on Splits

### 4. Transaction - Audit Trail

```java
public class Transaction {
    private String id;
    private User from;
    private User to;
    private double amount;
    private TransactionType type;  // EXPENSE or SETTLEMENT
    private LocalDateTime timestamp;
    private String description;
}
```

---

## Design Patterns Used

### 1. Strategy Pattern - Split Calculation

**The Core Pattern of This Design**

```
┌─────────────────────────────────────────────────────────────────┐
│                    STRATEGY PATTERN                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│                  ┌─────────────────────┐                        │
│                  │   SplitStrategy     │                        │
│                  │    <<interface>>    │                        │
│                  │                     │                        │
│                  │ + split()           │                        │
│                  │ + validate()        │                        │
│                  └──────────┬──────────┘                        │
│                             │                                    │
│          ┌──────────────────┼──────────────────┐                │
│          │                  │                  │                │
│          ▼                  ▼                  ▼                │
│  ┌───────────────┐ ┌───────────────┐ ┌───────────────┐         │
│  │ EqualSplit    │ │ ExactSplit    │ │ PercentSplit  │         │
│  │ Strategy      │ │ Strategy      │ │ Strategy      │         │
│  │               │ │               │ │               │         │
│  │ $100 / 3      │ │ Sum must      │ │ Sum must      │         │
│  │ = $33.33 each │ │ equal $100    │ │ equal 100%    │         │
│  └───────────────┘ └───────────────┘ └───────────────┘         │
│                                                                  │
│  Usage in SplitwiseService:                                     │
│  ─────────────────────────────                                   │
│  SplitStrategy strategy = strategies.get(splitType);           │
│  List<Split> splits = strategy.split(amount, participants);    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**Benefits:**
- ✅ Adding new split type = add new class
- ✅ Each strategy independently testable
- ✅ No if-else chains in main code
- ✅ Open-Closed Principle

### 2. Singleton Pattern - SplitwiseService

```java
public class SplitwiseService {
    private static SplitwiseService instance;
    
    private SplitwiseService() { }
    
    public static synchronized SplitwiseService getInstance() {
        if (instance == null) {
            instance = new SplitwiseService();
        }
        return instance;
    }
}
```

**Why Singleton?**
- Central management of users, groups, balances
- Consistent state across all operations
- In production: Replace with dependency injection

### 3. Factory Pattern (Implicit) - Strategy Selection

```java
// In SplitwiseService constructor
private Map<SplitType, SplitStrategy> strategies;

strategies.put(SplitType.EQUAL, new EqualSplitStrategy());
strategies.put(SplitType.EXACT, new ExactSplitStrategy());
strategies.put(SplitType.PERCENTAGE, new PercentageSplitStrategy());

// Usage - Factory-like lookup
SplitStrategy strategy = strategies.get(splitType);
```

---

## Core Algorithm: Balance Management

### The Balance Update Logic

```
┌─────────────────────────────────────────────────────────────────┐
│              BALANCE UPDATE ALGORITHM                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  SCENARIO: Alice pays $120 for dinner, split equally (3 people)│
│                                                                  │
│  Step 1: Calculate splits                                       │
│  ─────────────────────────                                       │
│  $120 / 3 = $40 each                                            │
│  Splits: [Alice: $40, Bob: $40, Carol: $40]                    │
│                                                                  │
│  Step 2: Update balances for each split                        │
│  ─────────────────────────────────────────                       │
│                                                                  │
│  For Alice (payer = participant):                              │
│  → Skip! Alice doesn't owe herself                             │
│                                                                  │
│  For Bob ($40):                                                 │
│  → Alice.balances["Bob"] += $40    (Bob owes Alice $40)        │
│  → Bob.balances["Alice"] -= $40    (Bob owes Alice $40)        │
│                                                                  │
│  For Carol ($40):                                               │
│  → Alice.balances["Carol"] += $40  (Carol owes Alice $40)      │
│  → Carol.balances["Alice"] -= $40  (Carol owes Alice $40)      │
│                                                                  │
│  RESULT:                                                         │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Alice.balances = { "Bob": +40, "Carol": +40 }            │   │
│  │ Bob.balances   = { "Alice": -40 }                        │   │
│  │ Carol.balances = { "Alice": -40 }                        │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  Reading: Positive = they owe me, Negative = I owe them        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### The Settlement Logic

```
┌─────────────────────────────────────────────────────────────────┐
│              SETTLEMENT ALGORITHM                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  SCENARIO: Bob settles $40 with Alice                          │
│                                                                  │
│  Before:                                                         │
│  Alice.balances["Bob"] = +40   (Bob owes Alice $40)            │
│  Bob.balances["Alice"] = -40   (Bob owes Alice $40)            │
│                                                                  │
│  Settlement: Bob pays Alice $40                                 │
│  ───────────────────────────────                                 │
│  Bob.updateBalance("Alice", +40)   → -40 + 40 = 0              │
│  Alice.updateBalance("Bob", -40)   → +40 - 40 = 0              │
│                                                                  │
│  After:                                                          │
│  Alice.balances["Bob"] = 0 → REMOVED (cleanup zero balances)   │
│  Bob.balances["Alice"] = 0 → REMOVED                           │
│                                                                  │
│  Both are now settled up!                                       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Balance Invariant

```
INVARIANT: For any two users A and B:
    A.balances[B] = -B.balances[A]
    
Example:
    Alice.balances["Bob"] = +40
    Bob.balances["Alice"] = -40
    
    +40 = -(-40) ✓
```

---

## Concurrency & Thread Safety

### Thread-Safe Collections Used

```java
// In User.java
private Map<String, Double> balances = new ConcurrentHashMap<>();

// In Group.java
private List<User> members = new CopyOnWriteArrayList<>();
private List<Expense> expenses = new CopyOnWriteArrayList<>();

// In SplitwiseService.java
private Map<String, User> users = new ConcurrentHashMap<>();
private Map<String, Group> groups = new ConcurrentHashMap<>();
private List<Transaction> transactions = new CopyOnWriteArrayList<>();
```

### Synchronized Methods

```java
// In User.java
public synchronized void updateBalance(String userId, double amount) {
    // Prevents race condition when two expenses update same user pair
}

// In SplitwiseService.java
public synchronized Expense addExpense(...) {
    // Ensures atomic expense creation and balance update
}

public synchronized Transaction settleUp(...) {
    // Ensures atomic settlement
}
```

### Race Condition Prevention

```
┌─────────────────────────────────────────────────────────────────┐
│              RACE CONDITION SCENARIO                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  WITHOUT SYNCHRONIZATION:                                       │
│                                                                  │
│  Thread 1: Alice pays $100        Thread 2: Bob pays $50       │
│  (Bob owes $50)                   (Alice owes $25)             │
│                                                                  │
│  1. Read Alice.balances["Bob"]=0  1. Read Bob.balances["Alice"]=0
│  2. Calculate: 0 + 50 = 50        2. Calculate: 0 + 25 = 25    │
│  3. Write: Alice["Bob"] = 50      3. Write: Bob["Alice"] = 25  │
│                                                                  │
│  RESULT: Balances are WRONG!                                    │
│  Alice thinks Bob owes $50, Bob thinks Alice owes $25          │
│  But they should net out to Bob owes $25!                      │
│                                                                  │
│  ──────────────────────────────────────────────────────────────  │
│                                                                  │
│  WITH SYNCHRONIZATION:                                          │
│                                                                  │
│  addExpense() is synchronized                                   │
│  → Only one expense processed at a time                        │
│  → Balances always consistent                                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Key Design Decisions & Trade-offs

### Decision 1: Balance in User vs Separate Table

| Approach | Pros | Cons |
|----------|------|------|
| **In User (Our choice)** | O(1) lookup, simple API | Denormalized, sync needed |
| Separate Balance entity | Normalized, auditable | O(n) queries |
| Calculate from expenses | Always accurate | O(all_expenses) |

**Staff Answer:** "We chose in-user balances for performance. In a real app with DB, I'd use a separate balance table with triggers or event-driven updates."

### Decision 2: Mutable Split vs Immutable

```java
// Current: Mutable Split created by strategy
Split split = new Split(user, amount);

// Alternative: Immutable with builder
Split split = Split.builder()
    .user(user)
    .amount(amount)
    .build();
```

**Our Choice:** Mutable for simplicity. In production, prefer immutable for thread safety.

### Decision 3: Strategy per Request vs Singleton Strategies

```java
// Current: Singleton strategies (reused)
strategies.put(SplitType.EQUAL, new EqualSplitStrategy());

// Alternative: Create new strategy per request
SplitStrategy strategy = SplitStrategyFactory.create(splitType);
```

**Our Choice:** Singleton strategies because they're stateless. No benefit to creating new instances.

### Decision 4: Cleanup Zero Balances

```java
public synchronized void updateBalance(String userId, double amount) {
    double newBalance = current + amount;
    
    // Clean up zero balances
    if (Math.abs(newBalance) < 0.01) {
        balances.remove(userId);  // Remove instead of storing 0
    }
}
```

**Why?**
- Cleaner data (no clutter of $0.00 entries)
- Faster iteration over non-zero balances
- "All settled up!" detection is easy

---

## Edge Cases & Failure Handling

### Edge Case Matrix

| Edge Case | How We Handle | Code Location |
|-----------|---------------|---------------|
| **Payer is participant** | Skip self in balance update | `updateBalances()` |
| **Split doesn't sum to total** | Validation fails | `strategy.validate()` |
| **Percentage doesn't sum to 100** | Validation fails | `PercentageSplitStrategy` |
| **Floating-point errors** | 0.01 tolerance | `Math.abs(diff) < 0.01` |
| **Zero balance cleanup** | Remove from map | `updateBalance()` |
| **Partial group split** | Only update participants | Explicit participant map |
| **Personal expense (no group)** | Pass null groupId | `addPersonalExpense()` |
| **Negative amounts** | Should validate | Add validation |
| **User not found** | Return null/empty | `getUser()` returns null |

### Floating-Point Precision

```java
// Problem: $100 / 3 = $33.333333...
// Three shares: 33.33 + 33.33 + 33.33 = $99.99 (1 cent missing!)

// Current: We tolerate small errors
if (Math.abs(sum - amount) < 0.01) { /* valid */ }

// Production solution: Use BigDecimal
BigDecimal amount = new BigDecimal("100.00");
BigDecimal share = amount.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
// Last person gets remainder
```

### Validation Implementation

```java
// ExactSplitStrategy
@Override
public boolean validate(double amount, Map<String, Double> participants) {
    double sum = participants.values().stream()
        .mapToDouble(Double::doubleValue).sum();
    return Math.abs(sum - amount) < 0.01;  // Tolerance for float errors
}

// PercentageSplitStrategy
@Override
public boolean validate(double amount, Map<String, Double> participants) {
    double sum = participants.values().stream()
        .mapToDouble(Double::doubleValue).sum();
    return Math.abs(sum - 100.0) < 0.01;  // Must sum to 100%
}
```

---

## Extensibility & Future Enhancements

### Adding New Split Types

```java
// 1. Create new strategy
public class WeightedSplitStrategy implements SplitStrategy {
    @Override
    public List<Split> split(double amount, Map<String, Double> weights, 
                            Map<String, User> users) {
        double totalWeight = weights.values().stream().mapToDouble(d -> d).sum();
        List<Split> splits = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            double share = (entry.getValue() / totalWeight) * amount;
            splits.add(new Split(users.get(entry.getKey()), share));
        }
        return splits;
    }
}

// 2. Add to enum
enum SplitType { EQUAL, EXACT, PERCENTAGE, WEIGHTED }

// 3. Register in service
strategies.put(SplitType.WEIGHTED, new WeightedSplitStrategy());

// No other code changes needed!
```

### Future Features

| Feature | Changes Required | Difficulty |
|---------|------------------|------------|
| **Weighted split** | Add WeightedSplitStrategy | 🟢 Easy |
| **Recurring expenses** | Add scheduler, ExpenseTemplate | 🟡 Medium |
| **Currency conversion** | Add CurrencyService | 🟡 Medium |
| **Simplify debts** | Add debt simplification algorithm | 🟡 Medium |
| **Expense categories** | Add Category enum to Expense | 🟢 Easy |
| **Notifications** | Add Observer pattern | 🟡 Medium |
| **Expense deletion/edit** | Reverse balance changes | 🟡 Medium |

### Debt Simplification Algorithm

```
┌─────────────────────────────────────────────────────────────────┐
│              DEBT SIMPLIFICATION                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Before simplification:                                         │
│  A owes B $10                                                   │
│  B owes C $10                                                   │
│  C owes A $10                                                   │
│  (3 transactions)                                               │
│                                                                  │
│  After simplification:                                          │
│  No one owes anyone!                                            │
│  (0 transactions - circular debt cancels out)                  │
│                                                                  │
│  Algorithm:                                                      │
│  1. Calculate net balance for each person                       │
│  2. Separate into creditors (+) and debtors (-)                │
│  3. Match debtors to creditors to minimize transactions        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Interview Discussion Points

### Q1: "Why use Strategy pattern instead of simple if-else?"

**Answer:**
"If-else works but violates Open-Closed Principle. Every new split type requires modifying the core addExpense method. With Strategy:
1. New split type = new class, existing code untouched
2. Each strategy is independently testable
3. Can swap strategies at runtime
4. Cleaner, more maintainable code"

### Q2: "How would you handle expense deletion?"

**Answer:**
"Deletion requires reversing the balance changes:

```java
public synchronized void deleteExpense(Expense expense) {
    User payer = expense.getPaidBy();
    
    for (Split split : expense.getSplits()) {
        if (!split.getUser().equals(payer)) {
            // Reverse the balance update
            payer.updateBalance(split.getUser().getId(), -split.getAmount());
            split.getUser().updateBalance(payer.getId(), split.getAmount());
        }
    }
    
    // Remove from group
    group.removeExpense(expense);
    
    // Add delete transaction for audit
    transactions.add(new Transaction(..., TransactionType.DELETION));
}
```

Key considerations:
- Need to store original expense data (we do in Expense object)
- Transaction log for audit trail
- Handle partial deletions for edited expenses"

### Q3: "What if two users add expenses at the same time?"

**Answer:**
"We use `synchronized` on `addExpense()` method:
- Only one expense can be processed at a time
- Prevents race conditions on balance updates

For higher throughput:
1. Lock per user pair, not global lock
2. Use optimistic locking with version numbers
3. Use a message queue to serialize updates"

### Q4: "How would you persist this to a database?"

**Answer:**
```sql
-- Users table
CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100)
);

-- Balances table (derived, but cached for performance)
CREATE TABLE balances (
    user_id VARCHAR(50),
    other_user_id VARCHAR(50),
    amount DECIMAL(10, 2),
    PRIMARY KEY (user_id, other_user_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Expenses table
CREATE TABLE expenses (
    expense_id VARCHAR(50) PRIMARY KEY,
    amount DECIMAL(10, 2),
    description VARCHAR(255),
    paid_by VARCHAR(50),
    split_type VARCHAR(20),
    created_at TIMESTAMP
);

-- Splits table
CREATE TABLE splits (
    split_id VARCHAR(50) PRIMARY KEY,
    expense_id VARCHAR(50),
    user_id VARCHAR(50),
    amount DECIMAL(10, 2),
    percentage DECIMAL(5, 2)
);

-- Transactions table (audit log)
CREATE TABLE transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    from_user VARCHAR(50),
    to_user VARCHAR(50),
    amount DECIMAL(10, 2),
    type VARCHAR(20),
    description VARCHAR(255),
    created_at TIMESTAMP
);
```

### Q5: "How would you implement recurring expenses?"

**Answer:**
"Add ExpenseTemplate and Scheduler:

```java
public class ExpenseTemplate {
    private String id;
    private double amount;
    private String description;
    private User paidBy;
    private SplitType splitType;
    private Map<String, Double> participants;
    private RecurrenceRule recurrence; // DAILY, WEEKLY, MONTHLY
    private LocalDate nextDue;
}

// Scheduler job
@Scheduled(cron = "0 0 0 * * *") // Daily midnight
public void processRecurringExpenses() {
    List<ExpenseTemplate> dueTemplates = templateRepo.findByNextDueBefore(today);
    
    for (ExpenseTemplate template : dueTemplates) {
        addExpense(template.toExpense());
        template.setNextDue(template.calculateNextDue());
    }
}
```"

---

## Quick Reference Cheat Sheet

```
┌─────────────────────────────────────────────────────────────────┐
│              SPLITWISE LLD - INTERVIEW CHEAT SHEET              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  DESIGN PATTERNS:                                                │
│  ✓ Strategy Pattern  → SplitStrategy (Equal, Exact, Percentage)│
│  ✓ Singleton Pattern → SplitwiseService                        │
│  ✓ Factory (implicit)→ Strategy selection from map             │
│                                                                  │
│  CORE DATA STRUCTURES:                                          │
│  ✓ User.balances     → Map<userId, amount> (who owes me)       │
│  ✓ Group.members     → List<User>                               │
│  ✓ Expense.splits    → List<Split>                              │
│                                                                  │
│  BALANCE SEMANTICS:                                              │
│  ✓ Positive          → They owe me                              │
│  ✓ Negative          → I owe them                               │
│  ✓ Invariant         → A.balance[B] = -B.balance[A]            │
│                                                                  │
│  CONCURRENCY:                                                    │
│  ✓ ConcurrentHashMap → Thread-safe maps                        │
│  ✓ CopyOnWriteArrayList → Thread-safe lists                    │
│  ✓ synchronized methods → Atomic expense/settlement            │
│                                                                  │
│  KEY TRADE-OFFS:                                                 │
│  ✓ Balance in User   → Fast O(1) lookup vs normalized DB       │
│  ✓ Singleton strategies → Stateless, reusable                  │
│  ✓ Zero cleanup      → Remove $0 balances for cleanliness      │
│                                                                  │
│  EDGE CASES:                                                     │
│  ✓ Payer is participant → Skip self in balance update          │
│  ✓ Float precision   → 0.01 tolerance                          │
│  ✓ Partial split     → Only update listed participants         │
│  ✓ Personal expense  → groupId = null                          │
│                                                                  │
│  EXTENSIBILITY:                                                  │
│  ✓ New split type    → Just add new Strategy class             │
│  ✓ Recurring expense → Add template + scheduler                 │
│  ✓ Debt simplification → Net balance algorithm                 │
│                                                                  │
│  COMPLEXITY:                                                     │
│  ✓ addExpense()      → O(n) where n = participants             │
│  ✓ getBalance()      → O(1) per user pair                      │
│  ✓ settleUp()        → O(1)                                    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Summary: What Makes This Design Staff-Level?

| Aspect | How We Demonstrate It |
|--------|----------------------|
| **SOLID Principles** | Strategy pattern (OCP), Single responsibility per class |
| **Design Patterns** | Strategy, Singleton, Factory - all with clear justification |
| **Concurrency** | Thread-safe collections, synchronized methods |
| **Trade-off Analysis** | Balance storage, strategy instantiation, precision handling |
| **Edge Cases** | Float errors, zero cleanup, payer-as-participant |
| **Extensibility** | Clear path to add split types, recurring, simplification |
| **Production Awareness** | DB schema, BigDecimal, audit logging |


# Coffee Vending Machine - Staff-Level LLD Design Document

## Table of Contents
1. [Problem Statement](#problem-statement)
2. [Requirements Analysis](#requirements-analysis)
3. [High-Level Architecture](#high-level-architecture)
4. [Design Patterns Deep Dive](#design-patterns-deep-dive)
5. [Class-by-Class Breakdown](#class-by-class-breakdown)
6. [State Machine Flow](#state-machine-flow)
7. [Edge Cases & Error Handling](#edge-cases--error-handling)
8. [Design Trade-offs](#design-trade-offs)
9. [Interview Complexity Assessment](#interview-complexity-assessment)
10. [Common Interview Questions](#common-interview-questions)
11. [Quick Reference](#quick-reference)

---

## Problem Statement

Design a Coffee Vending Machine that:
- Dispenses various beverages (Espresso, Latte, etc.)
- Accepts customizations (add-ons like milk, sugar)
- Handles payment and change
- Manages ingredient inventory
- Prevents invalid state transitions

---

## Requirements Analysis

### Functional Requirements

| Requirement | Pattern Used |
|-------------|--------------|
| Multiple beverage types | Decorator Pattern |
| Add-ons (milk, sugar) dynamically | Decorator Pattern |
| Machine states (idle, payment, dispensing) | State Pattern |
| Single machine instance | Singleton Pattern |
| Ingredient management | Inventory class |
| Payment handling | State transitions |

### Non-Functional Requirements

| Requirement | Solution |
|-------------|----------|
| Thread-safety for inventory | ConcurrentHashMap |
| Extensibility for new drinks | Decorator/Interface |
| Easy to add new states | State Pattern |

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      COFFEE MACHINE LLD                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    CoffeeMachine                         │   │
│  │                     (Singleton)                          │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │   │
│  │  │  IdleState  │→ │PaymentState │→ │DispensingState│    │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │   │
│  │                    (State Pattern)                       │   │
│  └────────────────────────┬────────────────────────────────┘   │
│                           │                                     │
│  ┌────────────────────────▼────────────────────────────────┐   │
│  │                      Beverage                            │   │
│  │                    (Interface)                           │   │
│  │  ┌──────────┐                                           │   │
│  │  │ Espresso │  ← Concrete Beverage                      │   │
│  │  └────┬─────┘                                           │   │
│  │       │ wrapped by                                      │   │
│  │  ┌────▼─────┐  ┌────────┐                              │   │
│  │  │   Milk   │→ │ Sugar  │  ← Decorators                │   │
│  │  └──────────┘  └────────┘                              │   │
│  │                (Decorator Pattern)                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Inventory                             │   │
│  │            (Ingredient Management)                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Design Patterns Deep Dive

### Pattern 1: State Pattern ⭐

**Problem**: Machine behavior depends on current state. Same action (like `insertMoney()`) should behave differently in Idle vs Payment state.

**Solution**: Encapsulate state-specific behavior in separate state classes.

```
┌─────────────────────────────────────────────────────────────┐
│                    STATE PATTERN                             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌─────────────┐    selectBeverage()    ┌─────────────┐   │
│   │  IdleState  │ ─────────────────────> │PaymentState │   │
│   └─────────────┘                        └──────┬──────┘   │
│         ↑                                       │           │
│         │              insertMoney()            │           │
│         │            (enough money)             ▼           │
│   ┌─────┴───────────────────────────── ┌─────────────┐     │
│   │                                    │DispensingState│    │
│   │               dispense()           └─────────────┘     │
│   └────────────────────────────────────────────────────────│
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

**Why State Pattern vs If-Else?**

```java
// BAD: If-else spaghetti
public void insertMoney(double amount) {
    if (state == IDLE) {
        System.out.println("Select beverage first");
    } else if (state == PAYMENT) {
        balance += amount;
        if (balance >= cost) {
            state = DISPENSING;
            dispense();
        }
    } else if (state == DISPENSING) {
        System.out.println("Please wait...");
    }
}

// GOOD: State Pattern - each state handles its own logic
public class PaymentState implements MachineState {
    public void insertMoney(double amount) {
        machine.addBalance(amount);
        if (machine.getCurrentBalance() >= machine.getCurrentBeverage().getCost()) {
            machine.setState(machine.getDispensingState());
            machine.dispense();
        }
    }
}
```

**Interview Quote**: "State Pattern eliminates complex conditionals. Each state class encapsulates its own behavior. Adding a new state (like MaintenanceState) requires adding a class, not modifying existing if-else chains."

---

### Pattern 2: Decorator Pattern ⭐

**Problem**: How to handle customizations like "Espresso + Milk + Sugar" without creating a class for every combination?

**Without Decorator** (Bad):
```java
// Explosion of classes!
class Espresso { }
class EspressoWithMilk { }
class EspressoWithSugar { }
class EspressoWithMilkAndSugar { }
class EspressoWithDoubleMilk { }
// ... 50+ combinations
```

**With Decorator** (Good):
```java
Beverage latte = new Espresso();              // Base: $2.00
latte = new Milk(latte);                       // + Milk: $0.50
latte = new Sugar(latte);                      // + Sugar: $0.25
// Total: $2.75, Description: "Espresso, Milk, Sugar"
```

**Visual**:
```
┌─────────────────────────────────────────────────────────────┐
│                   DECORATOR PATTERN                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌────────────┐                                            │
│   │ <<interface>>                                           │
│   │  Beverage  │                                            │
│   │ getCost()  │                                            │
│   │ getDesc()  │                                            │
│   └─────┬──────┘                                            │
│         │                                                    │
│    ┌────┴────────────────┐                                  │
│    │                     │                                  │
│    ▼                     ▼                                  │
│ ┌──────────┐    ┌───────────────────┐                      │
│ │ Espresso │    │ CondimentDecorator│                      │
│ │ (Base)   │    │    (Abstract)     │                      │
│ │ $2.00    │    │ holds Beverage    │                      │
│ └──────────┘    └────────┬──────────┘                      │
│                          │                                  │
│                 ┌────────┴────────┐                        │
│                 │                 │                        │
│                 ▼                 ▼                        │
│            ┌────────┐       ┌────────┐                     │
│            │  Milk  │       │ Sugar  │                     │
│            │ +$0.50 │       │ +$0.25 │                     │
│            └────────┘       └────────┘                     │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

**Key Insight**: Decorators implement `Beverage` AND hold a `Beverage`. This allows infinite nesting.

---

### Pattern 3: Singleton Pattern

**Problem**: We want exactly ONE coffee machine instance.

```java
public class CoffeeMachine {
    private static CoffeeMachine instance;
    
    private CoffeeMachine() {
        // Private constructor
    }
    
    public static synchronized CoffeeMachine getInstance() {
        if (instance == null) {
            instance = new CoffeeMachine();
        }
        return instance;
    }
}
```

**Why Singleton Here?**
1. Physical constraint: One machine exists
2. Shared inventory: All requests use same inventory
3. State consistency: Single source of truth for machine state

**Thread-Safety**: `synchronized` ensures thread-safe lazy initialization.

---

## Class-by-Class Breakdown

### 1. Beverage (Interface)

```java
public interface Beverage {
    String getDescription();
    double getCost();
    List<String> getIngredients();  // For inventory check
}
```

**Why `getIngredients()`?**: Allows inventory to check if all required ingredients are available.

### 2. Espresso (Concrete Beverage)

```java
public class Espresso implements Beverage {
    public String getDescription() { return "Espresso"; }
    public double getCost() { return 2.00; }
    public List<String> getIngredients() {
        return Arrays.asList("CoffeeBeans", "Water");
    }
}
```

### 3. CondimentDecorator (Abstract Decorator)

```java
public abstract class CondimentDecorator implements Beverage {
    protected Beverage beverage;  // Wrapped beverage
    
    public CondimentDecorator(Beverage beverage) {
        this.beverage = beverage;
    }
}
```

**Key Point**: Implements `Beverage` (IS-A) and holds `Beverage` (HAS-A).

### 4. Milk (Concrete Decorator)

```java
public class Milk extends CondimentDecorator {
    public Milk(Beverage beverage) {
        super(beverage);
    }
    
    public String getDescription() {
        return beverage.getDescription() + ", Milk";  // Delegate + Add
    }
    
    public double getCost() {
        return beverage.getCost() + 0.50;  // Delegate + Add
    }
    
    public List<String> getIngredients() {
        List<String> ingredients = new ArrayList<>(beverage.getIngredients());
        ingredients.add("Milk");
        return ingredients;
    }
}
```

### 5. MachineState (Interface)

```java
public interface MachineState {
    void selectBeverage(Beverage beverage);
    void insertMoney(double amount);
    void dispense();
}
```

### 6. IdleState

```java
public class IdleState implements MachineState {
    private CoffeeMachine machine;
    
    public void selectBeverage(Beverage beverage) {
        // Check inventory BEFORE accepting selection
        if (!machine.getInventory().checkIngredients(beverage.getIngredients())) {
            System.out.println("Out of Stock");
            return;  // Stay in Idle
        }
        
        machine.setCurrentBeverage(beverage);
        machine.setState(machine.getPaymentState());  // Transition
    }
    
    public void insertMoney(double amount) {
        System.out.println("Please select a beverage first.");
    }
    
    public void dispense() {
        System.out.println("Please select a beverage first.");
    }
}
```

### 7. PaymentState

```java
public class PaymentState implements MachineState {
    public void selectBeverage(Beverage beverage) {
        System.out.println("Already selected " + machine.getCurrentBeverage());
    }
    
    public void insertMoney(double amount) {
        machine.addBalance(amount);
        
        if (machine.getCurrentBalance() >= machine.getCurrentBeverage().getCost()) {
            machine.setState(machine.getDispensingState());
            machine.dispense();  // Auto-trigger
        }
    }
    
    public void dispense() {
        System.out.println("Please insert full amount.");
    }
}
```

### 8. DispensingState

```java
public class DispensingState implements MachineState {
    public void selectBeverage(Beverage b) {
        System.out.println("Please wait, dispensing...");
    }
    
    public void insertMoney(double amount) {
        System.out.println("Please wait, dispensing...");
    }
    
    public void dispense() {
        // Consume ingredients
        machine.getInventory().consume(machine.getCurrentBeverage().getIngredients());
        
        // Return change
        double change = machine.getCurrentBalance() - machine.getCurrentBeverage().getCost();
        if (change > 0) {
            System.out.println("Returning change: $" + change);
        }
        
        // Reset and transition to Idle
        machine.resetBalance();
        machine.setCurrentBeverage(null);
        machine.setState(machine.getIdleState());
    }
}
```

### 9. Inventory

```java
public class Inventory {
    private Map<String, Integer> ingredients = new ConcurrentHashMap<>();
    
    public boolean checkIngredients(String... required) {
        for (String item : required) {
            if (ingredients.getOrDefault(item, 0) <= 0) return false;
        }
        return true;
    }
    
    public void consume(String... required) {
        for (String item : required) {
            ingredients.computeIfPresent(item, (k, v) -> v - 1);
        }
    }
    
    public void refill(String item, int amount) {
        ingredients.merge(item, amount, Integer::sum);
    }
}
```

### 10. CoffeeMachine (Singleton + Context)

```java
public class CoffeeMachine {
    private static CoffeeMachine instance;
    private MachineState currentState;
    private Inventory inventory;
    private Beverage currentBeverage;
    private double currentBalance;
    
    // Pre-created states (avoids creating new objects)
    private MachineState idleState;
    private MachineState paymentState;
    private MachineState dispensingState;
    
    private CoffeeMachine() {
        inventory = new Inventory();
        idleState = new IdleState(this);
        paymentState = new PaymentState(this);
        dispensingState = new DispensingState(this);
        currentState = idleState;
    }
    
    // Actions delegated to current state
    public void selectBeverage(Beverage b) { currentState.selectBeverage(b); }
    public void insertMoney(double amt) { currentState.insertMoney(amt); }
    public void dispense() { currentState.dispense(); }
}
```

---

## State Machine Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    STATE TRANSITIONS                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌──────────────────────────────────────────────────────────┐ │
│   │                                                           │ │
│   │  ┌─────────┐   selectBeverage()    ┌─────────────┐      │ │
│   │  │  IDLE   │ ───────────────────── │  PAYMENT    │      │ │
│   │  │         │   (if in stock)       │             │      │ │
│   │  └────┬────┘                       └──────┬──────┘      │ │
│   │       │                                   │              │ │
│   │       │ selectBeverage()                  │ insertMoney()│ │
│   │       │ (out of stock)                    │ (enough)     │ │
│   │       │                                   │              │ │
│   │       ▼                                   ▼              │ │
│   │  [Stay IDLE]                       ┌─────────────┐      │ │
│   │                                    │ DISPENSING  │      │ │
│   │                                    │             │      │ │
│   │                                    └──────┬──────┘      │ │
│   │                                           │              │ │
│   │                                           │ dispense()   │ │
│   │                                           │              │ │
│   │       ┌───────────────────────────────────┘              │ │
│   │       │                                                  │ │
│   │       ▼                                                  │ │
│   │  ┌─────────┐                                            │ │
│   │  │  IDLE   │  ← Back to start                           │ │
│   │  └─────────┘                                            │ │
│   │                                                           │ │
│   └──────────────────────────────────────────────────────────┘ │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Edge Cases & Error Handling

### 1. Out of Stock

```java
// Checked in IdleState BEFORE transitioning
public void selectBeverage(Beverage beverage) {
    if (!machine.getInventory().checkIngredients(...)) {
        System.out.println("Out of Stock");
        return;  // Stay in Idle - don't transition!
    }
    // Proceed...
}
```

### 2. Insufficient Payment

```java
// PaymentState only transitions when enough money
public void insertMoney(double amount) {
    machine.addBalance(amount);
    if (machine.getCurrentBalance() >= machine.getCurrentBeverage().getCost()) {
        machine.setState(machine.getDispensingState());
        machine.dispense();
    }
    // Otherwise, stay in PaymentState
}
```

### 3. Invalid Actions in Current State

Each state handles invalid actions gracefully:

```java
// IdleState
public void insertMoney(double amount) {
    System.out.println("Please select a beverage first.");
}

// DispensingState
public void selectBeverage(Beverage b) {
    System.out.println("Please wait, dispensing...");
}
```

### 4. Change Calculation

```java
double change = machine.getCurrentBalance() - machine.getCurrentBeverage().getCost();
if (change > 0) {
    System.out.println("Returning change: $" + change);
}
```

### 5. Cancel Order (Not Implemented - Extension)

Would add a `cancel()` method to return money and go back to Idle.

---

## Design Trade-offs

### Trade-off 1: Pre-created States vs On-demand

| Approach | Pros | Cons |
|----------|------|------|
| **Pre-created (Our Choice)** | No object creation per transition | Memory for unused states |
| On-demand | Memory efficient | GC pressure, object creation overhead |

**Why We Win**: States are lightweight. Pre-creation avoids GC and is faster.

### Trade-off 2: Decorator vs Inheritance

| Approach | Pros | Cons |
|----------|------|------|
| **Decorator (Our Choice)** | Infinite combinations | Slightly complex |
| Inheritance | Simple | Class explosion (2^n combinations) |

**Why We Win**: Adding "Caramel" requires ONE class, not doubling all combinations.

### Trade-off 3: State Pattern vs Enum + Switch

| Approach | Pros | Cons |
|----------|------|------|
| **State Pattern (Our Choice)** | OCP compliant, testable | More classes |
| Enum + Switch | Simple, compact | Violates OCP, hard to extend |

**Why We Win**: Adding "MaintenanceState" = new class. No existing code modified.

---

## Interview Complexity Assessment

### Current Implementation Stats

| Metric | Count | Verdict |
|--------|-------|---------|
| Total Files | 12 | ⚠️ Moderate |
| Total Lines | ~200 | ✅ Manageable |
| Patterns Used | 3 | ✅ Shows depth |
| Core Logic Lines | ~100 | ✅ Good |

### Is This Right for Interview?

**Yes, but with prioritization**:

| Priority | What to Write | Time | Why |
|----------|---------------|------|-----|
| 1 | State Pattern (3 states) | 15 min | Shows state management |
| 2 | Decorator Pattern (1 decorator) | 10 min | Shows extensibility |
| 3 | CoffeeMachine + Beverage | 10 min | Core structure |
| 4 | Inventory (if time) | 5 min | Nice to have |

### Minimum Viable Implementation (~120 lines)

```java
// 1. Beverage interface (5 lines)
interface Beverage {
    String getDescription();
    double getCost();
}

// 2. Espresso (10 lines)
class Espresso implements Beverage { ... }

// 3. MachineState interface (5 lines)
interface MachineState {
    void selectBeverage(Beverage b);
    void insertMoney(double amt);
    void dispense();
}

// 4. Three states (45 lines total)
class IdleState implements MachineState { ... }
class PaymentState implements MachineState { ... }
class DispensingState implements MachineState { ... }

// 5. CoffeeMachine (35 lines)
class CoffeeMachine {
    private MachineState currentState;
    private Beverage currentBeverage;
    private double balance;
    
    // State getters
    // Action delegation
}

// 6. One Decorator (20 lines)
abstract class CondimentDecorator implements Beverage { ... }
class Milk extends CondimentDecorator { ... }
```

### What to Skip in Time Crunch

1. ❌ `getIngredients()` in Beverage (complex)
2. ❌ Inventory class (nice to have)
3. ❌ Sugar decorator (Milk is enough to show pattern)
4. ❌ Singleton (mention but don't implement)

---

## Common Interview Questions

### Q1: "Why State Pattern instead of if-else?"

**Answer**: "If-else leads to complex conditionals that grow with each new state. State Pattern encapsulates state-specific behavior. Adding a new state like 'MaintenanceState' means adding a class, not modifying existing code - following Open/Closed Principle."

### Q2: "Why Decorator over inheritance for beverages?"

**Answer**: "With inheritance, adding milk and sugar would require classes for every combination: EspressoWithMilk, EspressoWithSugar, EspressoWithMilkAndSugar, etc. That's 2^n classes for n condiments. Decorator allows runtime composition - just wrap decorators as needed."

### Q3: "How would you add a new state like Maintenance?"

**Answer**:
```java
public class MaintenanceState implements MachineState {
    public void selectBeverage(Beverage b) {
        System.out.println("Machine under maintenance");
    }
    // ... other methods
}

// In CoffeeMachine
private MachineState maintenanceState = new MaintenanceState(this);
public void enterMaintenance() { setState(maintenanceState); }
```

### Q4: "How would you add a cancel feature?"

**Answer**: "Add `cancel()` to MachineState interface. In PaymentState, return the balance and transition to Idle. In other states, it's a no-op or error message."

### Q5: "Is this thread-safe?"

**Answer**: "Partially. Inventory uses ConcurrentHashMap. But CoffeeMachine itself isn't thread-safe - multiple users could interleave operations. For production, I'd synchronize state transitions or use a queue for requests."

### Q6: "Why pre-create state objects?"

**Answer**: "States are stateless (they hold reference to machine, but machine state is in machine). Creating them once avoids object creation per transition, reducing GC pressure and improving performance."

---

## Quick Reference

### Patterns Summary

```
┌────────────────────────────────────────────────────────────────┐
│                    PATTERNS USED                                │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. STATE PATTERN                                              │
│     • Why: Machine behavior depends on current state           │
│     • How: IdleState, PaymentState, DispensingState            │
│     • Benefit: Easy to add new states, OCP compliant           │
│                                                                 │
│  2. DECORATOR PATTERN                                          │
│     • Why: Flexible beverage customization                     │
│     • How: Milk, Sugar wrap Espresso                           │
│     • Benefit: Avoid class explosion, runtime composition      │
│                                                                 │
│  3. SINGLETON PATTERN                                          │
│     • Why: One machine instance                                │
│     • How: Private constructor + static getInstance()          │
│     • Benefit: Global access point, shared state               │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

### State Transitions

```
IDLE ──selectBeverage()──> PAYMENT ──insertMoney(enough)──> DISPENSING
  ↑                                                              │
  └────────────────────── dispense() ────────────────────────────┘
```

### Decorator Composition

```java
Beverage drink = new Sugar(new Milk(new Espresso()));
// Description: "Espresso, Milk, Sugar"
// Cost: 2.00 + 0.50 + 0.25 = $2.75
```

### Key Interview Points

1. **State Pattern**: Encapsulates state-specific behavior, OCP compliant
2. **Decorator Pattern**: Avoids class explosion, runtime composition
3. **Inventory Check**: Done in IdleState BEFORE transitioning
4. **Auto-dispense**: PaymentState triggers dispense when enough money
5. **Pre-created States**: Avoids object creation per transition

---

## Final Interview Quote

> "This design uses **State Pattern** for clean state management - no if-else chains, and adding states is OCP compliant. **Decorator Pattern** handles customizations without class explosion - adding a new condiment is one class, not 2^n combinations. The machine checks inventory **before** accepting selection, preventing wasted user effort. States are **pre-created** to avoid GC pressure during operation."


# Elevator System - Low-Level Design Document

## Staff-Level Interview Reference Guide

---

## 1. Executive Summary

This document presents a **production-ready Elevator System** demonstrating:
- **State Pattern** for elevator behavior (Idle, MovingUp, MovingDown)
- **Strategy Pattern** for pluggable dispatch algorithms
- **Active Object Pattern** for concurrent elevator operations
- **Singleton Pattern** for centralized controller

**Key Differentiator**: This design handles **real-time concurrent requests** with each elevator operating as an independent thread, making it suitable for production deployment.

---

## 2. The Problem: Why Elevators Are Hard

Before diving into code, understand why elevator systems are a classic interview problem:

```
┌─────────────────────────────────────────────────────────────┐
│                    THE CHALLENGES                           │
├─────────────────────────────────────────────────────────────┤
│ 1. CONCURRENCY: Multiple users press buttons simultaneously │
│ 2. STATE: Elevator can be idle, moving up, moving down     │
│ 3. SCHEDULING: Which elevator should respond to a request? │
│ 4. OPTIMIZATION: Minimize wait time, energy, wear          │
│ 5. REAL-TIME: Must respond immediately, can't "batch"      │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        EXTERNAL WORLD                               │
│     [User presses button on Floor 3, wants to go to Floor 7]       │
└────────────────────────────┬────────────────────────────────────────┘
                             │ requestElevator(3, 7)
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    ElevatorController (Singleton)                   │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ • Receives all external requests                             │   │
│  │ • Uses DispatchStrategy to select best elevator              │   │
│  │ • Forwards request to selected elevator                      │   │
│  └─────────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────────┘
                             │ addRequest(Request)
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    DispatchStrategy (Interface)                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ selectElevator(List<Elevator>, Request) → Elevator           │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                              △                                       │
│              ┌───────────────┴───────────────┐                      │
│         FCFSStrategy              NearestElevatorStrategy           │
│        (First Come)                  (Shortest Distance)            │
└─────────────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│              Elevator (Active Object - Runs in Thread)              │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ • Has its own thread (implements Runnable)                   │   │
│  │ • Maintains queue of pending requests                        │   │
│  │ • Current state determines behavior                          │   │
│  └─────────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────────┘
                             │ state.move(this)
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    ElevatorState (Interface)                        │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ move(Elevator): What to do on each tick                      │   │
│  │ stop(Elevator): How to handle stopping                       │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                              △                                       │
│          ┌───────────────────┼───────────────────┐                  │
│     IdleState          MovingUpState        MovingDownState         │
│   (waiting for        (going up,          (going down,              │
│    requests)           floor by floor)     floor by floor)          │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 4. Design Patterns Deep Dive

### 4.1 State Pattern - The Heart of the System

**Problem**: Elevator behavior changes based on its current state:
- When **Idle**: Check for requests, decide direction
- When **Moving Up**: Go up one floor, check if should stop
- When **Moving Down**: Go down one floor, check if should stop

**Without State Pattern** (Bad):
```java
// ❌ Spaghetti code with complex conditionals
public void move() {
    if (state == IDLE) {
        if (!requests.isEmpty()) {
            if (nextRequest.floor > currentFloor) {
                state = MOVING_UP;
            } else {
                state = MOVING_DOWN;
            }
        }
    } else if (state == MOVING_UP) {
        currentFloor++;
        if (hasRequestAtFloor(currentFloor)) {
            state = IDLE;
            openDoors();
        }
    } else if (state == MOVING_DOWN) {
        // ... more nested conditions
    }
}
```

**With State Pattern** (Good):
```java
// ✓ Clean, each state knows its own behavior
public interface ElevatorState {
    void move(Elevator elevator);
    void stop(Elevator elevator);
}

// In Elevator:
public void move() {
    state.move(this);  // Delegate to current state
}
```

**State Transition Diagram**:
```
                    ┌──────────────────┐
                    │                  │
                    ▼                  │
              ┌──────────┐             │
     ┌───────►│  IDLE    │◄────────┐   │
     │        └────┬─────┘         │   │
     │             │               │   │
     │    Request  │  Request      │   │
     │    above    │  below        │   │
     │             ▼               │   │
     │     ┌───────────────┐       │   │
     │     │  MOVING_UP    │───────┘   │
     │     └───────────────┘           │
     │        arrived at               │
     │        destination              │
     │             │                   │
     │             │    Request        │
     │             │    above          │
     │             ▼                   │
     │     ┌───────────────┐           │
     └─────│ MOVING_DOWN   │───────────┘
           └───────────────┘
              arrived at
              destination
```

**Implementation**:

```java
// IdleState.java - Decides which direction to go
public class IdleState implements ElevatorState {
    @Override
    public void move(Elevator elevator) {
        if (elevator.getPendingRequests().isEmpty()) {
            return;  // Stay idle
        }
        
        Request nextReq = elevator.getPendingRequests().get(0);
        
        if (nextReq.getSourceFloor() > elevator.getCurrentFloor()) {
            elevator.setState(new MovingUpState());  // Transition!
        } else if (nextReq.getSourceFloor() < elevator.getCurrentFloor()) {
            elevator.setState(new MovingDownState()); // Transition!
        } else {
            // Already at floor - open doors
            elevator.getPendingRequests().remove(0);
        }
    }
}

// MovingUpState.java - Goes up floor by floor
public class MovingUpState implements ElevatorState {
    @Override
    public void move(Elevator elevator) {
        int next = elevator.getCurrentFloor() + 1;
        elevator.setCurrentFloor(next);
        
        // Check if we need to stop at this floor
        if (elevator.hasRequestAt(next)) {
            elevator.setState(new IdleState());  // Transition!
            elevator.stop();  // Open doors, remove request
        }
    }
}
```

**Interview Talking Point**:
> "I used State Pattern because elevator behavior is fundamentally state-dependent. Each state encapsulates its own logic, making the code maintainable. Adding a new state like 'MaintenanceMode' or 'EmergencyStop' requires adding one class without modifying existing code - Open/Closed Principle."

---

### 4.2 Active Object Pattern - Concurrency Model

**Problem**: Each elevator must operate independently and respond to requests in real-time.

**Solution**: Each elevator is a **thread** that runs continuously, waiting for requests.

```java
public class Elevator implements Runnable {
    @Override
    public void run() {
        while (true) {  // Infinite loop - elevator never stops
            synchronized (this) {
                // WAIT if no requests (don't burn CPU)
                while (pendingRequests.isEmpty()) {
                    wait();  // Sleep until notified
                }
            }
            
            move();  // Process requests
            Thread.sleep(500);  // Simulate travel time
        }
    }
    
    public synchronized void addRequest(Request request) {
        pendingRequests.add(request);
        notifyAll();  // WAKE UP the waiting elevator!
    }
}
```

**Why This Pattern?**

| Without Active Object | With Active Object |
|----------------------|-------------------|
| Single thread handles all elevators | Each elevator has own thread |
| Elevators can't move simultaneously | True parallel operation |
| One stuck elevator blocks all | Fault isolation |
| Complex coordination logic | Simple per-elevator logic |

**Thread Lifecycle**:

```
Elevator Thread Created
        │
        ▼
┌───────────────────┐
│  wait() - Sleeping│◄──────────────────────┐
│  (No CPU usage)   │                       │
└─────────┬─────────┘                       │
          │                                 │
          │ notifyAll() when               │
          │ request added                   │
          ▼                                 │
┌───────────────────┐                       │
│  Process Request  │                       │
│  move() called    │                       │
└─────────┬─────────┘                       │
          │                                 │
          │ Request complete                │
          │ & no more requests              │
          └─────────────────────────────────┘
```

**Interview Talking Point**:
> "I used Active Object pattern because elevators are inherently concurrent - they operate independently. The `wait()/notifyAll()` mechanism ensures threads sleep when idle (no CPU waste) and wake immediately when requests arrive. This models real elevator behavior accurately."

---

### 4.3 Strategy Pattern - Pluggable Dispatch Algorithms

**Problem**: Different buildings need different elevator dispatch policies:
- **FCFS**: Simple, fair, but not optimal
- **Nearest**: Minimize travel distance
- **LOOK/SCAN**: Elevator-specific, like disk scheduling
- **Zone-based**: Express elevators for high floors

**Solution**: Define dispatch as a strategy interface.

```java
public interface DispatchStrategy {
    Elevator selectElevator(List<Elevator> elevators, Request request);
}

// Simple First-Come-First-Served
public class FCFSStrategy implements DispatchStrategy {
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        return elevators.get(0);  // Always pick first elevator
    }
}

// Better: Nearest elevator
public class NearestStrategy implements DispatchStrategy {
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        Elevator nearest = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Elevator e : elevators) {
            int distance = Math.abs(e.getCurrentFloor() - request.getSourceFloor());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = e;
            }
        }
        return nearest;
    }
}
```

**Switching Strategies at Runtime**:
```java
// Configure based on building type
if (building.isHighRise()) {
    controller.setStrategy(new ZoneBasedStrategy());
} else {
    controller.setStrategy(new NearestStrategy());
}
```

**Interview Talking Point**:
> "Strategy Pattern decouples the dispatch algorithm from the controller. We can switch from FCFS to Nearest or LOOK algorithm without changing any other code. This is critical for real buildings where dispatch needs might change based on time of day (rush hour vs. night) or building type."

---

### 4.4 Singleton Pattern - Central Controller

```java
public class ElevatorController {
    private static ElevatorController instance;
    
    private ElevatorController(int numElevators) {
        // Initialize elevators
    }
    
    public static synchronized getInstance(int numElevators) {
        if (instance == null) {
            instance = new ElevatorController(numElevators);
        }
        return instance;
    }
}
```

**Why Singleton?**
- One controller coordinates all elevators
- Prevents conflicting dispatch decisions
- Single point of entry for all requests

---

## 5. Request Flow - Step by Step

Let's trace a request through the system:

```
User on Floor 0 wants to go to Floor 6
```

### Step 1: Controller Receives Request

```java
controller.requestElevator(0, 6);
```

### Step 2: Create Request Object

```java
Request req = new Request(0, 6);
// req.sourceFloor = 0
// req.destinationFloor = 6
// req.direction = UP
```

### Step 3: Strategy Selects Elevator

```java
Elevator selected = strategy.selectElevator(elevators, req);
// FCFS returns elevators.get(0) = Elevator 1
```

### Step 4: Add Request to Elevator

```java
selected.addRequest(req);
// This calls notifyAll() - waking the elevator thread!
```

### Step 5: Elevator Wakes Up

```java
// In Elevator.run():
while (pendingRequests.isEmpty()) {
    wait();  // Was sleeping here
}
// Request added → loop exits
move();  // Start processing
```

### Step 6: IdleState Decides Direction

```java
// IdleState.move():
if (nextReq.getSourceFloor() > currentFloor) {  // 0 > 0? No
    // ...
} else if (nextReq.getSourceFloor() < currentFloor) {  // 0 < 0? No
    // ...
} else {
    // Already at source floor! Pick up passenger
    // But now need to go to destination...
}
```

### Step 7: Moving Up

```java
// MovingUpState.move():
currentFloor++;  // 0 → 1 → 2 → 3 → 4 → 5 → 6

if (hasRequestAt(6)) {  // Yes!
    setState(new IdleState());
    stop();  // Open doors, remove request
}
```

---

## 6. Concurrency Deep Dive

### 6.1 The synchronized Keyword

Every method that accesses shared state is synchronized:

```java
public synchronized void addRequest(Request request) { ... }
public synchronized void move() { ... }
public synchronized int getCurrentFloor() { ... }
```

**Why?** Multiple threads access the elevator:
- Main thread adds requests
- Elevator's own thread processes them
- Controller thread might query state

### 6.2 The wait/notify Mechanism

```java
// Elevator thread waits when no requests
synchronized (this) {
    while (pendingRequests.isEmpty()) {
        wait();  // Releases lock, sleeps
    }
}

// Controller wakes elevator when request added
public synchronized void addRequest(Request request) {
    pendingRequests.add(request);
    notifyAll();  // Wakes waiting threads
}
```

**Critical Understanding**:
- `wait()` releases the lock AND sleeps
- `notifyAll()` wakes sleeping threads
- Woken thread must re-acquire lock before continuing
- Use `while` not `if` to check condition (spurious wakeups)

---

## 7. Edge Cases & Failure Handling

### 7.1 Edge Cases Handled

| Edge Case | How Handled |
|-----------|-------------|
| Request to current floor | IdleState removes request immediately |
| Multiple requests same floor | `hasRequestAt()` checks all requests |
| No elevators available | Strategy must handle (return any) |
| Request while moving | Added to queue, processed in order |
| Empty request queue | `wait()` until request arrives |

### 7.2 Potential Issues & Fixes

**Issue 1: Request Starvation**

Current code processes first request, ignoring others on the way:
```java
// Problem: Going from 0 to 6, ignores request at floor 3
Request nextReq = pendingRequests.get(0);  // Only looks at first
```

**Fix**: LOOK/SCAN algorithm picks up passengers on the way:
```java
// Better: Stop at any floor with a request in our direction
if (hasRequestInDirection(currentFloor, direction)) {
    stop();
}
```

**Issue 2: Source vs Destination**

Current `hasRequestAt()` checks both source AND destination:
```java
if (r.getSourceFloor() == floor || r.getDestinationFloor() == floor)
```

This is correct! Elevator stops to:
1. **Pick up** passengers (source floor)
2. **Drop off** passengers (destination floor)

**Issue 3: Request Not Removed After Source Stop**

When elevator arrives at **source floor**, it picks up the passenger but shouldn't remove the request yet - the request is complete only at the **destination**.

Current code removes on any match, which might cause issues. A cleaner approach:
```java
public void stopAtSource(int floor) {
    for (Request r : pendingRequests) {
        if (r.getSourceFloor() == floor) {
            r.setPickedUp(true);  // Mark, don't remove
        }
    }
}

public void stopAtDestination(int floor) {
    pendingRequests.removeIf(r -> 
        r.isPickedUp() && r.getDestinationFloor() == floor
    );
}
```

---

## 8. Alternative Approaches Comparison

### 8.1 Dispatch Algorithms

| Algorithm | Pros | Cons | Best For |
|-----------|------|------|----------|
| **FCFS** | Simple, fair | Inefficient | Low-traffic buildings |
| **Nearest** | Less wait time | May cause starvation | Small buildings |
| **LOOK/SCAN** | Efficient, predictable | More complex | High-rise buildings |
| **Zone-based** | Express service | Underutilized elevators | Skyscrapers |

### 8.2 Concurrency Models

| Model | Our Approach | Alternative: Event Loop |
|-------|-------------|-------------------------|
| Threads | One per elevator | Single thread, event-driven |
| Pros | True parallelism, simple logic | Less overhead, no sync issues |
| Cons | Synchronization needed | Can't utilize multi-core |
| Best for | Production systems | Simulations |

---

## 9. Class Responsibilities (SOLID)

| Class | Responsibility | SOLID Principle |
|-------|---------------|-----------------|
| `ElevatorController` | Coordinate requests, select elevator | Single Responsibility |
| `Elevator` | Maintain state, process requests | Single Responsibility |
| `ElevatorState` | Define behavior interface | Interface Segregation |
| `IdleState/MovingUpState/etc.` | Implement specific behaviors | Open/Closed |
| `DispatchStrategy` | Define selection interface | Dependency Inversion |
| `FCFSStrategy` | Implement one algorithm | Single Responsibility |

---

## 10. Interview-Ready Q&A

### Q1: "Why State Pattern instead of simple enums?"

> "Enums require switch/if-else chains that grow with each state. State Pattern encapsulates behavior IN the state class. When I add 'MaintenanceState' or 'EmergencyState', I create one new class without touching existing code. This follows Open/Closed Principle."

### Q2: "How would you handle elevator maintenance?"

> "Add a `MaintenanceState` that:
> 1. Rejects new requests
> 2. Completes current passengers
> 3. Returns to ground floor
> 4. Stays idle until reset
> 
> The controller's strategy would exclude elevators in MaintenanceState."

### Q3: "Your FCFS always picks elevator 1. How would you improve?"

> "I'd implement NearestElevatorStrategy:
> ```java
> public Elevator selectElevator(List<Elevator> elevators, Request req) {
>     return elevators.stream()
>         .min(Comparator.comparingInt(e -> 
>             Math.abs(e.getCurrentFloor() - req.getSourceFloor())
>         ))
>         .orElse(elevators.get(0));
> }
> ```
> Or LOOK algorithm that considers elevator direction too."

### Q4: "How do you prevent race conditions?"

> "All shared state access is synchronized:
> 1. `addRequest()` is synchronized - safe concurrent adds
> 2. `move()` is synchronized - state changes are atomic
> 3. `getCurrentFloor()` is synchronized - consistent reads
> 4. `wait()/notifyAll()` for producer-consumer coordination
> 
> The elevator's thread and the controller's thread never see inconsistent state."

### Q5: "What happens if an elevator breaks down mid-transit?"

> "I'd add:
> 1. **Health check**: Each elevator reports status periodically
> 2. **Timeout detection**: If no movement for X seconds, mark as failed
> 3. **Request redistribution**: Controller reassigns pending requests to other elevators
> 4. **Emergency state**: Elevator stops and waits for maintenance
> 
> This requires the controller to track elevator health, not just dispatch."

### Q6: "How would you scale this to 100 elevators?"

> "The current design scales well because:
> 1. Each elevator is independent (own thread)
> 2. Strategy can implement complex zone logic
> 3. Controller is stateless (just dispatches)
> 
> For 100 elevators, I'd:
> 1. **Zone the building**: Floors 1-20, 21-40, etc.
> 2. **Partition elevators**: Each zone has dedicated elevators
> 3. **Add load balancing**: Track queue lengths in strategy
> 4. **Consider thread pools**: Instead of 100 threads, use work-stealing pool"

---

## 11. Sequence Diagram

```
User          Controller        Strategy         Elevator          State
  │                │                │               │                │
  │  request(0,6)  │                │               │                │
  │───────────────►│                │               │                │
  │                │ selectElevator │               │                │
  │                │───────────────►│               │                │
  │                │   elevator 1   │               │                │
  │                │◄───────────────│               │                │
  │                │                                │                │
  │                │           addRequest           │                │
  │                │───────────────────────────────►│                │
  │                │                                │                │
  │                │                                │  notifyAll()   │
  │                │                                │ (wakes thread) │
  │                │                                │                │
  │                │                                │    move()      │
  │                │                                │───────────────►│
  │                │                                │                │
  │                │                                │   (IdleState)  │
  │                │                                │   decide dir   │
  │                │                                │◄───────────────│
  │                │                                │                │
  │                │                                │  setState(Up)  │
  │                │                                │───────────────►│
  │                │                                │                │
  │                │                                │    move()      │
  │                │                                │───────────────►│
  │                │                                │                │
  │                │                                │ (MovingUpState)│
  │                │                                │ floor 0→1→2→...│
  │                │                                │◄───────────────│
  │                │                                │                │
  │                │                                │  at floor 6    │
  │                │                                │  setState(Idle)│
  │                │                                │───────────────►│
  │                │                                │                │
  │                │                                │    stop()      │
  │                │                                │ remove request │
  │                │                                │◄───────────────│
```

---

## 12. Code Quality Highlights

### 12.1 Thread Safety

```java
// All shared state access is synchronized
public synchronized void addRequest(Request request) {
    pendingRequests.add(request);
    notifyAll();
}
```

### 12.2 State Encapsulation

```java
// Elevator doesn't know HOW to move, just THAT it should
public void move() {
    state.move(this);  // Delegate to state
}
```

### 12.3 Strategy Flexibility

```java
// Controller doesn't know HOW to select, just THAT it should
Elevator selected = strategy.selectElevator(elevators, req);
```

---

## 13. Testing Strategy

```java
// Unit Tests
@Test void testIdleToMovingUpTransition() { ... }
@Test void testMovingUpStopsAtRequestFloor() { ... }
@Test void testMultipleRequestsProcessedInOrder() { ... }

// Integration Tests
@Test void testTwoElevatorsHandleConcurrentRequests() { ... }
@Test void testElevatorWakesOnNewRequest() { ... }

// Stress Tests
@Test void test100ConcurrentRequests() { ... }
```

---

## 14. Summary: Why This Design Wins

| Aspect | Design Decision | Benefit |
|--------|-----------------|---------|
| **Behavior** | State Pattern | Clean state transitions, extensible |
| **Concurrency** | Active Object | True parallelism, real-time response |
| **Dispatch** | Strategy Pattern | Pluggable algorithms, easy optimization |
| **Coordination** | Singleton Controller | Single point of truth |
| **Thread Safety** | synchronized + wait/notify | Safe concurrent access |

**Closing Statement for Interview**:
> "This elevator design demonstrates mastery of multiple patterns working together. State Pattern handles the inherently stateful nature of elevators. Active Object pattern enables real concurrent operation. Strategy Pattern allows optimizing dispatch without touching core logic. The `wait()/notifyAll()` mechanism ensures efficient resource usage - elevators sleep when idle, wake instantly when needed. This isn't just textbook patterns - it's how you'd actually build a production elevator control system."


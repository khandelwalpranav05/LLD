# Elevator System - Interview Implementation Guide

## How to Implement in 30-45 Minutes

This guide shows a **simplified but complete** elevator system you can write in an interview.

---

## Step 1: Draw the Class Diagram First (2 minutes)

```
┌─────────────────┐       ┌─────────────────┐
│ ElevatorController│◄────│  DispatchStrategy │
│   (Singleton)    │       └─────────────────┘
└────────┬────────┘                │
         │                    FCFSStrategy
         │ has many
         ▼
┌─────────────────┐       ┌─────────────────┐
│    Elevator     │◄────│   ElevatorState   │
│  (Runnable)     │       └─────────────────┘
└────────┬────────┘           △   △   △
         │              Idle  Up  Down
         │ has many
         ▼
┌─────────────────┐
│    Request      │
└─────────────────┘
```

**Tell interviewer**: "I'll use State Pattern for elevator behavior and Strategy Pattern for dispatch."

---

## Step 2: Start with Enums and Simple Classes (3 minutes)

```java
// Direction.java
public enum Direction {
    UP, DOWN, IDLE
}

// Request.java
public class Request {
    int source;
    int destination;
    Direction direction;
    boolean pickedUp = false;
    
    public Request(int src, int dest) {
        this.source = src;
        this.destination = dest;
        this.direction = dest > src ? Direction.UP : Direction.DOWN;
    }
}
```

---

## Step 3: State Interface (1 minute)

```java
// ElevatorState.java
public interface ElevatorState {
    void move(Elevator elevator);
}
```

---

## Step 4: Elevator Class - The Core (10 minutes)

```java
public class Elevator implements Runnable {
    int id;
    int currentFloor = 0;
    Direction direction = Direction.IDLE;
    ElevatorState state = new IdleState();
    List<Request> requests = new ArrayList<>();
    
    public Elevator(int id) { this.id = id; }
    
    // Thread loop
    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                while (requests.isEmpty()) {
                    try { wait(); } catch (Exception e) {}
                }
            }
            state.move(this);
            try { Thread.sleep(500); } catch (Exception e) {}
        }
    }
    
    // Add request (called by controller)
    public synchronized void addRequest(Request r) {
        requests.add(r);
        notifyAll();
    }
    
    // Open doors - pickup and dropoff
    public synchronized void openDoors() {
        System.out.println("Elevator " + id + " DOORS OPEN at " + currentFloor);
        
        // Pickup: Mark waiting passengers as picked up
        for (Request r : requests) {
            if (!r.pickedUp && r.source == currentFloor && 
                (direction == Direction.IDLE || r.direction == direction)) {
                r.pickedUp = true;
                System.out.println("  Picked up: " + r.source + "→" + r.destination);
            }
        }
        
        // Dropoff: Remove passengers at destination
        requests.removeIf(r -> r.pickedUp && r.destination == currentFloor);
    }
    
    // Should we stop at this floor?
    public synchronized boolean shouldStop(int floor) {
        for (Request r : requests) {
            // Dropoff
            if (r.pickedUp && r.destination == floor) return true;
            // Pickup (same direction)
            if (!r.pickedUp && r.source == floor &&
                (direction == Direction.IDLE || r.direction == direction)) return true;
        }
        return false;
    }
    
    // Any destinations above?
    public synchronized boolean hasAbove() {
        for (Request r : requests) {
            int target = r.pickedUp ? r.destination : r.source;
            if (target > currentFloor) return true;
        }
        return false;
    }
    
    // Any destinations below?
    public synchronized boolean hasBelow() {
        for (Request r : requests) {
            int target = r.pickedUp ? r.destination : r.source;
            if (target < currentFloor) return true;
        }
        return false;
    }
}
```

---

## Step 5: State Implementations (8 minutes)

### IdleState - Decides direction

```java
public class IdleState implements ElevatorState {
    @Override
    public void move(Elevator e) {
        if (e.requests.isEmpty()) return;
        
        // Handle current floor first
        if (e.shouldStop(e.currentFloor)) {
            e.openDoors();
        }
        
        // Decide direction
        if (e.hasAbove()) {
            e.direction = Direction.UP;
            e.state = new MovingUpState();
        } else if (e.hasBelow()) {
            e.direction = Direction.DOWN;
            e.state = new MovingDownState();
        }
    }
}
```

### MovingUpState - Goes up, stops when needed

```java
public class MovingUpState implements ElevatorState {
    @Override
    public void move(Elevator e) {
        e.currentFloor++;
        System.out.println("Elevator " + e.id + " at floor " + e.currentFloor);
        
        if (e.shouldStop(e.currentFloor)) {
            e.openDoors();
        }
        
        // What's next?
        if (e.hasAbove()) {
            // Keep going up
        } else if (e.hasBelow()) {
            e.direction = Direction.DOWN;
            e.state = new MovingDownState();
        } else if (e.requests.isEmpty()) {
            e.direction = Direction.IDLE;
            e.state = new IdleState();
        }
    }
}
```

### MovingDownState - Mirror of Up

```java
public class MovingDownState implements ElevatorState {
    @Override
    public void move(Elevator e) {
        e.currentFloor--;
        System.out.println("Elevator " + e.id + " at floor " + e.currentFloor);
        
        if (e.shouldStop(e.currentFloor)) {
            e.openDoors();
        }
        
        if (e.hasBelow()) {
            // Keep going down
        } else if (e.hasAbove()) {
            e.direction = Direction.UP;
            e.state = new MovingUpState();
        } else if (e.requests.isEmpty()) {
            e.direction = Direction.IDLE;
            e.state = new IdleState();
        }
    }
}
```

---

## Step 6: Controller and Strategy (5 minutes)

```java
// DispatchStrategy.java
public interface DispatchStrategy {
    Elevator select(List<Elevator> elevators, Request request);
}

// FCFSStrategy.java (or NearestStrategy)
public class FCFSStrategy implements DispatchStrategy {
    public Elevator select(List<Elevator> elevators, Request req) {
        // Simple: return first elevator
        // Better: return nearest elevator
        return elevators.stream()
            .min(Comparator.comparingInt(e -> 
                Math.abs(e.currentFloor - req.source)))
            .orElse(elevators.get(0));
    }
}

// ElevatorController.java
public class ElevatorController {
    private static ElevatorController instance;
    private List<Elevator> elevators = new ArrayList<>();
    private DispatchStrategy strategy = new FCFSStrategy();
    
    private ElevatorController(int count) {
        for (int i = 0; i < count; i++) {
            Elevator e = new Elevator(i + 1);
            elevators.add(e);
            new Thread(e).start();
        }
    }
    
    public static synchronized ElevatorController getInstance(int count) {
        if (instance == null) instance = new ElevatorController(count);
        return instance;
    }
    
    public void request(int source, int destination) {
        Request req = new Request(source, destination);
        Elevator selected = strategy.select(elevators, req);
        selected.addRequest(req);
    }
}
```

---

## Total: ~6 Classes, ~150 Lines

| Class | Lines | Purpose |
|-------|-------|---------|
| Direction | 3 | Enum |
| Request | 10 | Data holder |
| ElevatorState | 3 | Interface |
| Elevator | 60 | Core logic |
| IdleState | 15 | Initial state |
| MovingUpState | 20 | Going up |
| MovingDownState | 20 | Going down |
| DispatchStrategy | 3 | Interface |
| FCFSStrategy | 8 | Simple selection |
| ElevatorController | 20 | Coordinator |

---

## Key Points to Remember

### 1. The LOOK Algorithm (Elevator Behavior)

```
GOING UP:
├── Move up one floor
├── Stop if: someone to pickup (going UP) OR someone to dropoff
├── Continue if more destinations above
├── Reverse if only destinations below
└── Idle if no requests left

GOING DOWN:
├── Same logic, opposite direction
```

### 2. Pickup vs Dropoff

```java
// Pickup: At SOURCE floor, mark as picked up
if (r.source == currentFloor && !r.pickedUp) {
    r.pickedUp = true;
}

// Dropoff: At DESTINATION floor, remove request
if (r.destination == currentFloor && r.pickedUp) {
    requests.remove(r);
}
```

### 3. Direction Matters for Pickup

```java
// Only pickup passengers going SAME direction
if (r.direction == elevator.direction || elevator.direction == IDLE) {
    r.pickedUp = true;
}
```

### 4. Thread Safety Pattern

```java
// Wait when no work
synchronized (this) {
    while (requests.isEmpty()) {
        wait();
    }
}

// Wake up when work added
public synchronized void addRequest(Request r) {
    requests.add(r);
    notifyAll();
}
```

---

## Interview Walkthrough Script

**Start with**: "I'll design an elevator system using State Pattern for elevator behavior and Strategy Pattern for dispatch."

**Draw diagram** (30 seconds)

**Implement in order**:
1. Enums (Direction)
2. Request class
3. ElevatorState interface
4. Elevator class (the meat)
5. Three state implementations
6. Strategy interface + simple implementation
7. Controller

**Test case to trace**: "If we request 0→6, 3→8, 5→0, the elevator goes 0→3→6→8→5→0"

---

## Common Interview Questions

**Q: Why State Pattern?**
> "Elevator behavior depends on state - idle, moving up, moving down. Each state has different logic. State pattern encapsulates this cleanly."

**Q: Why not just use if-else?**
> "If-else grows complex with more states (maintenance, emergency). State pattern follows Open/Closed - add new state without modifying existing."

**Q: How does LOOK algorithm work?**
> "Elevator continues in one direction until no more stops, then reverses. Like disk scheduling. Prevents starvation and minimizes direction changes."

**Q: What about thread safety?**
> "All shared state access is synchronized. wait/notify for producer-consumer between controller and elevator threads."

---

## Minimum Viable Implementation (If Short on Time)

If you have only 20 minutes, skip Strategy pattern:

```java
// Just hardcode elevator selection
public void request(int src, int dest) {
    elevators.get(0).addRequest(new Request(src, dest));
}
```

And mention: "In production, I'd use Strategy pattern for pluggable dispatch algorithms."


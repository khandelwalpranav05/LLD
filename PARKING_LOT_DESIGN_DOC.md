# Parking Lot LLD - Staff-Level Design Document

## Table of Contents
1. [Problem Statement](#problem-statement)
2. [Current Implementation Analysis](#current-implementation-analysis)
3. [Design Patterns Used](#design-patterns-used)
4. [Class Design](#class-design)
5. [Concurrency Handling](#concurrency-handling)
6. [Interview Complexity Assessment](#interview-complexity-assessment)
7. [Simplified Version for Interview](#simplified-version-for-interview)
8. [Common Interview Questions](#common-interview-questions)

---

## Problem Statement

Design a Parking Lot System that:
- Supports multiple floors and spot types
- Handles vehicle entry/exit
- Calculates parking fees
- Manages spot allocation
- Handles concurrency (multiple gates)

---

## Current Implementation Analysis

### File Count: ~20 files (TOO COMPLEX for 45-min interview)

```
com.parkinglot/
├── enums/           (3 files)
│   ├── SpotType.java
│   ├── VehicleType.java
│   └── TicketStatus.java
├── model/           (14 files)
│   ├── Vehicle.java (abstract)
│   ├── Car.java, Truck.java, Bike.java
│   ├── ParkingSpot.java (abstract)
│   ├── CompactSpot.java, LargeSpot.java, etc.
│   ├── ParkingFloor.java
│   ├── Ticket.java
│   ├── Gate.java, EntryGate.java, ExitGate.java
├── service/
│   └── ParkingLot.java (Singleton)
├── strategy/        (4 files)
│   ├── ParkingAssignmentStrategy.java
│   ├── NearestFirstStrategy.java
│   ├── PricingStrategy.java
│   └── HourlyPricingStrategy.java
└── ParkingLotDemo.java
```

### Verdict: **Too complex for interview. Need simplified version.**

---

## Design Patterns Used

### 1. Singleton Pattern (ParkingLot)

```java
public class ParkingLot {
    private static ParkingLot instance;
    
    private ParkingLot() { }
    
    public static synchronized ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }
}
```

**Why**: One parking lot per system. Global access point.

### 2. Strategy Pattern (Spot Assignment + Pricing)

```java
public interface ParkingAssignmentStrategy {
    ParkingSpot findSpot(List<ParkingFloor> floors, VehicleType type);
}

public interface PricingStrategy {
    double calculatePrice(Ticket ticket);
}
```

**Why**: Algorithms can change (nearest first, most empty floor, hourly/daily pricing).

### 3. Template Method (Vehicle, ParkingSpot hierarchies)

```java
public abstract class Vehicle {
    protected VehicleType type;
}

public class Car extends Vehicle { }
public class Truck extends Vehicle { }
```

**Why**: Common behavior in base, specific types in subclasses.

---

## Concurrency Handling

### Problem: Multiple gates assigning same spot

```
Gate 1: getSpot() → Spot C1
Gate 2: getSpot() → Spot C1 (SAME SPOT!)
Both assign → CONFLICT!
```

### Solution: Multi-level protection

#### Level 1: ConcurrentLinkedDeque for spot queue
```java
Deque<ParkingSpot> spots = new ConcurrentLinkedDeque<>();
ParkingSpot spot = spots.poll();  // Atomic removal
```

#### Level 2: Synchronized assignVehicle
```java
public synchronized boolean assignVehicle(Vehicle vehicle) {
    if (!isFree) return false;  // Already taken
    this.currentVehicle = vehicle;
    this.isFree = false;
    return true;
}
```

---

## Interview Complexity Assessment

| Component | Current | Interview Version |
|-----------|---------|-------------------|
| Files | ~20 | **6** |
| Patterns | 3 (Singleton, Strategy x2, Template) | **1-2** (Singleton, maybe Strategy) |
| Inheritance | Vehicle hierarchy, Spot hierarchy | **Enums only** |
| Gates | Entry/Exit with complex flow | **Skip** |
| Concurrency | Full | **Basic or skip** |
| Pricing | Strategy pattern | **Simple or skip** |

---

## Simplified Version for Interview ⭐

### Target: 6 files, ~150 lines, 45 minutes

```
com.parkinglot.simple/
├── VehicleType.java    (enum - 5 lines)
├── SpotType.java       (enum - 5 lines)
├── Vehicle.java        (simple class - 15 lines)
├── ParkingSpot.java    (simple class - 25 lines)
├── ParkingLot.java     (main logic - 80 lines)
└── ParkingLotDemo.java (test - 30 lines)
```

### Key Simplifications

| Full Version | Simple Version |
|--------------|----------------|
| Abstract Vehicle + Car, Truck, Bike | Simple Vehicle class with type enum |
| Abstract ParkingSpot + CompactSpot, LargeSpot | Simple ParkingSpot class with type enum |
| ParkingFloor class | Skip - single level lot |
| Entry/Exit Gates | Skip - direct park/unpark |
| Strategy Pattern for assignment | Simple loop |
| Strategy Pattern for pricing | Skip or simple hourly |
| Ticket class | Optional - return spot ID |
| Concurrency | synchronized on spot only |

---

## Common Interview Questions

### Q1: "Why Singleton for ParkingLot?"
**Answer**: "One physical parking lot per system. Provides global access point. In distributed system, we'd use a service instead."

### Q2: "How do you handle two cars trying for same spot?"
**Answer**: "Two levels: 1) Atomic poll from ConcurrentLinkedDeque 2) Synchronized assignVehicle returns false if already taken."

### Q3: "How would you find nearest spot?"
**Answer**: "Strategy pattern. NearestFirstStrategy iterates floors in order. Could have MostEmptyFloorStrategy to distribute load."

### Q4: "How would you add EV charging spots?"
**Answer**: "Add ELECTRIC to SpotType enum. Strategy maps VehicleType.ELECTRIC to SpotType.ELECTRIC."

---

## Quick Reference: What to Write in Interview

### Priority 1: Core Classes (MUST - 20 mins)
1. `VehicleType` enum
2. `SpotType` enum
3. `Vehicle` class
4. `ParkingSpot` class
5. `ParkingLot` class

### Priority 2: If Time Permits (10 mins)
- `Ticket` class
- Simple pricing

### Priority 3: Mention but Don't Code
- Multiple floors
- Entry/Exit gates
- Full concurrency
- Strategy patterns

---

## Simplified Version - Complete Code

### File Structure (7 files, ~180 lines)

```
com.parkinglot.simple/
├── VehicleType.java    (5 lines)
├── SpotType.java       (5 lines)
├── Vehicle.java        (15 lines)
├── ParkingSpot.java    (40 lines)
├── Ticket.java         (25 lines)
├── ParkingLot.java     (140 lines)
└── SimpleParkingDemo.java
```

### 1. VehicleType.java (5 lines)

```java
public enum VehicleType {
    BIKE,
    CAR,
    TRUCK
}
```

### 2. SpotType.java (5 lines)

```java
public enum SpotType {
    SMALL,    // For bikes
    MEDIUM,   // For cars
    LARGE     // For trucks
}
```

### 3. Vehicle.java (15 lines)

```java
public class Vehicle {
    private String licensePlate;
    private VehicleType type;
    
    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }
    
    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }
}
```

### 4. ParkingSpot.java (40 lines)

```java
public class ParkingSpot {
    private String id;
    private SpotType type;
    private Vehicle currentVehicle;
    
    public ParkingSpot(String id, SpotType type) {
        this.id = id;
        this.type = type;
    }
    
    // SYNCHRONIZED for thread safety
    public synchronized boolean park(Vehicle vehicle) {
        if (currentVehicle != null) return false;
        this.currentVehicle = vehicle;
        return true;
    }
    
    public synchronized void unpark() {
        this.currentVehicle = null;
    }
    
    public boolean isAvailable() { return currentVehicle == null; }
    public String getId() { return id; }
    public SpotType getType() { return type; }
}
```

### 5. Ticket.java (25 lines)

```java
public class Ticket {
    private String id;
    private Vehicle vehicle;
    private ParkingSpot spot;
    private long entryTime;
    
    public Ticket(String id, Vehicle vehicle, ParkingSpot spot) {
        this.id = id;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = System.currentTimeMillis();
    }
    
    // Getters...
    
    public double getHoursParked() {
        long duration = System.currentTimeMillis() - entryTime;
        return Math.max(1, Math.ceil(duration / (1000.0 * 60 * 60)));
    }
}
```

### 6. ParkingLot.java (140 lines) - THE CORE

```java
public class ParkingLot {
    private static ParkingLot instance;
    
    private Map<SpotType, List<ParkingSpot>> spotsByType;
    private Map<String, Ticket> activeTickets;
    private Map<VehicleType, Double> hourlyRates;
    private int ticketCounter = 0;
    
    private ParkingLot() {
        spotsByType = new HashMap<>();
        activeTickets = new HashMap<>();
        hourlyRates = new HashMap<>();
        
        for (SpotType type : SpotType.values()) {
            spotsByType.put(type, new ArrayList<>());
        }
        
        // Default pricing
        hourlyRates.put(VehicleType.BIKE, 10.0);
        hourlyRates.put(VehicleType.CAR, 20.0);
        hourlyRates.put(VehicleType.TRUCK, 30.0);
    }
    
    public static synchronized ParkingLot getInstance() {
        if (instance == null) instance = new ParkingLot();
        return instance;
    }
    
    public void addSpot(ParkingSpot spot) {
        spotsByType.get(spot.getType()).add(spot);
    }
    
    // CORE: Park a vehicle
    public Ticket parkVehicle(Vehicle vehicle) {
        SpotType requiredType = getSpotTypeForVehicle(vehicle.getType());
        ParkingSpot spot = findAvailableSpot(requiredType);
        
        if (spot == null) return null;  // No spot
        if (!spot.park(vehicle)) return null;  // Race condition
        
        String ticketId = "T-" + (++ticketCounter);
        Ticket ticket = new Ticket(ticketId, vehicle, spot);
        activeTickets.put(vehicle.getLicensePlate(), ticket);
        return ticket;
    }
    
    // CORE: Unpark a vehicle
    public double unparkVehicle(Ticket ticket) {
        if (ticket == null) return -1;
        
        Ticket active = activeTickets.get(ticket.getVehicle().getLicensePlate());
        if (active == null) return -1;  // Already left
        
        double fee = ticket.getHoursParked() * hourlyRates.get(ticket.getVehicle().getType());
        ticket.getSpot().unpark();
        activeTickets.remove(ticket.getVehicle().getLicensePlate());
        return fee;
    }
    
    private SpotType getSpotTypeForVehicle(VehicleType type) {
        switch (type) {
            case BIKE:  return SpotType.SMALL;
            case CAR:   return SpotType.MEDIUM;
            case TRUCK: return SpotType.LARGE;
            default:    return SpotType.MEDIUM;
        }
    }
    
    private ParkingSpot findAvailableSpot(SpotType type) {
        for (ParkingSpot spot : spotsByType.get(type)) {
            if (spot.isAvailable()) return spot;
        }
        return null;
    }
}
```

---

## Demo Output

```
=== SIMPLE PARKING LOT DEMO ===

Total spots: 6
Available MEDIUM spots: 3

--- Parking Vehicles ---
Parked BIKE-001 at spot S1. Ticket: T-1
Parked CAR-001 at spot M1. Ticket: T-2
Parked CAR-002 at spot M2. Ticket: T-3
Parked TRUCK-001 at spot L1. Ticket: T-4

--- Capacity Test ---
No spot available for TRUCK

--- Unparking Vehicles ---
Unparked CAR-001. Duration: 1.0 hrs. Fee: $20.0

--- After Unpark ---
Parked TRUCK-002 at spot L1. Ticket: T-5

--- Edge Case: Reuse Ticket ---
Ticket not found or already used
```

---

## Interview Flow (45 mins)

| Time | Task | What to Write |
|------|------|---------------|
| 0-3 | Requirements | Clarify: spot types, vehicle types, pricing |
| 3-8 | Enums + Vehicle | VehicleType, SpotType, Vehicle class |
| 8-15 | ParkingSpot | With synchronized park/unpark |
| 15-25 | ParkingLot | Singleton, parkVehicle, unparkVehicle |
| 25-35 | Ticket + Pricing | If time permits |
| 35-45 | Discussion | Edge cases, extensions |

---

## Key Interview Points

### 1. Data Structure Choice
```java
Map<SpotType, List<ParkingSpot>> spotsByType
```
**Why**: O(1) lookup by spot type, then linear scan within type.

### 2. Thread Safety
```java
public synchronized boolean park(Vehicle v) {
    if (currentVehicle != null) return false;
    // ...
}
```
**Why**: Two entry gates might find same spot. synchronized prevents race condition.

### 3. Vehicle-to-Spot Mapping
```java
private SpotType getSpotTypeForVehicle(VehicleType type) {
    switch (type) {
        case BIKE:  return SpotType.SMALL;
        case CAR:   return SpotType.MEDIUM;
        case TRUCK: return SpotType.LARGE;
    }
}
```
**Why**: Clean separation. Easy to extend (add ELECTRIC → CHARGING).

### 4. Ticket Validation
```java
Ticket active = activeTickets.get(ticket.getVehicle().getLicensePlate());
if (active == null) return -1;  // Already left or never parked
```
**Why**: Prevents ticket reuse.

---

## Extensions to Mention (Don't Code)

| Extension | How to Add |
|-----------|------------|
| **Multiple Floors** | Add `ParkingFloor` class, iterate floors in `findSpot` |
| **Strategy Pattern** | Extract `ParkingAssignmentStrategy` interface |
| **Entry/Exit Gates** | Add `Gate` classes that call `parkVehicle/unparkVehicle` |
| **Reservations** | Add `reserveSpot()` method, `reserved` field in spot |
| **Display Board** | Observer pattern to show available spots per type |

---

## Final Interview Quote

> "This simplified design handles core requirements: parking/unparking vehicles with correct spot type matching, hourly pricing, and ticket validation. Thread safety is achieved through synchronized methods on ParkingSpot. For extensions like multiple floors or different pricing strategies, I would add a Strategy pattern - but the core remains simple and extensible."

package com.elevator.simple;

import java.util.ArrayList;
import java.util.List;

/**
 * SIMPLIFIED Elevator - Easy to write in interview
 * Uses public fields to reduce boilerplate
 */
public class Elevator implements Runnable {
    public int id;
    public int floor = 0;
    public Direction direction = Direction.IDLE;
    public ElevatorState state = new IdleState();
    public List<Request> requests = new ArrayList<>();
    
    public Elevator(int id) {
        this.id = id;
    }
    
    // ============ THREAD LOOP ============
    @Override
    public void run() {
        while (true) {
            // Wait if no requests
            synchronized (this) {
                while (requests.isEmpty()) {
                    try { wait(); } catch (Exception e) {}
                }
            }
            // Process one step
            state.move(this);
            try { Thread.sleep(500); } catch (Exception e) {}
        }
    }
    
    // ============ ADD REQUEST (Called by Controller) ============
    public synchronized void addRequest(Request r) {
        requests.add(r);
        System.out.println("E" + id + " received: " + r);
        notifyAll();  // Wake up the thread!
    }
    
    // ============ OPEN DOORS - Pickup & Dropoff ============
    public synchronized void openDoors() {
        System.out.println("E" + id + " *** DOORS OPEN at floor " + floor + " ***");
        
        // PICKUP: passengers waiting at this floor, going our direction
        for (Request r : requests) {
            if (!r.pickedUp && r.source == floor) {
                if (direction == Direction.IDLE || r.direction == direction) {
                    r.pickedUp = true;
                    System.out.println("  PICKUP: " + r);
                }
            }
        }
        
        // DROPOFF: passengers whose destination is this floor
        requests.removeIf(r -> {
            if (r.pickedUp && r.destination == floor) {
                System.out.println("  DROPOFF: " + r);
                return true;
            }
            return false;
        });
    }
    
    // ============ SHOULD STOP AT FLOOR? ============
    public synchronized boolean shouldStop(int f) {
        for (Request r : requests) {
            // Dropoff check
            if (r.pickedUp && r.destination == f) return true;
            // Pickup check (same direction only)
            if (!r.pickedUp && r.source == f) {
                if (direction == Direction.IDLE || r.direction == direction) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // ============ ANY DESTINATIONS ABOVE? ============
    public synchronized boolean hasAbove() {
        for (Request r : requests) {
            int target = r.pickedUp ? r.destination : r.source;
            if (target > floor) return true;
        }
        return false;
    }
    
    // ============ ANY DESTINATIONS BELOW? ============
    public synchronized boolean hasBelow() {
        for (Request r : requests) {
            int target = r.pickedUp ? r.destination : r.source;
            if (target < floor) return true;
        }
        return false;
    }
}


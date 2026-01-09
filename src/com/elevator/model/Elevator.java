package com.elevator.model;

import com.elevator.state.ElevatorState;
import com.elevator.state.IdleState;
import java.util.ArrayList;
import java.util.List;

public class Elevator implements Runnable {
    private int id;
    private int currentFloor;
    private Direction currentDirection;
    private ElevatorState state;
    private List<Request> pendingRequests;

    public Elevator(int id) {
        this.id = id;
        this.currentFloor = 0; // Ground Floor
        this.currentDirection = Direction.IDLE;
        this.state = new IdleState();
        this.pendingRequests = new ArrayList<>();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                while (pendingRequests.isEmpty()) {
                    try {
                        System.out.println("Elevator " + id + " waiting for requests...");
                        currentDirection = Direction.IDLE;
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            move();
            
            try { Thread.sleep(500); } catch (InterruptedException e) {}
        }
    }

    public synchronized void addRequest(Request request) {
        pendingRequests.add(request);
        System.out.println("Elevator " + id + " received request: " + request);
        notifyAll();
    }

    public synchronized void move() {
        state.move(this);
    }

    public synchronized void openDoors() {
        System.out.println("Elevator " + id + " *** DOORS OPEN at floor " + currentFloor + " ***");
        
        // Pick up passengers waiting at this floor (going in our direction)
        for (Request r : pendingRequests) {
            if (!r.isPickedUp() && r.getSourceFloor() == currentFloor) {
                // In LOOK algorithm, only pick up if going same direction OR we're idle
                if (currentDirection == Direction.IDLE || r.getDirection() == currentDirection) {
                    r.setPickedUp(true);
                    System.out.println("  → Picked up passenger: " + r);
                }
            }
        }
        
        // Drop off passengers whose destination is this floor
        List<Request> toRemove = new ArrayList<>();
        for (Request r : pendingRequests) {
            if (r.isPickedUp() && r.getDestinationFloor() == currentFloor) {
                System.out.println("  → Dropped off passenger: " + r);
                toRemove.add(r);
            }
        }
        pendingRequests.removeAll(toRemove);
        
        System.out.println("Elevator " + id + " *** DOORS CLOSE ***");
    }

    /**
     * Check if elevator should stop at this floor.
     * Stop if:
     * 1. Someone waiting here wants to go in our direction (pickup)
     * 2. Someone on board wants to get off here (dropoff)
     */
    public synchronized boolean shouldStopAt(int floor) {
        for (Request r : pendingRequests) {
            // Dropoff: passenger on board, destination is this floor
            if (r.isPickedUp() && r.getDestinationFloor() == floor) {
                return true;
            }
            // Pickup: passenger waiting, source is this floor, going same direction
            if (!r.isPickedUp() && r.getSourceFloor() == floor) {
                if (currentDirection == Direction.IDLE || r.getDirection() == currentDirection) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if there are any destinations above current floor
     */
    public synchronized boolean hasDestinationsAbove() {
        for (Request r : pendingRequests) {
            if (r.isPickedUp() && r.getDestinationFloor() > currentFloor) {
                return true;
            }
            if (!r.isPickedUp() && r.getSourceFloor() > currentFloor) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if there are any destinations below current floor
     */
    public synchronized boolean hasDestinationsBelow() {
        for (Request r : pendingRequests) {
            if (r.isPickedUp() && r.getDestinationFloor() < currentFloor) {
                return true;
            }
            if (!r.isPickedUp() && r.getSourceFloor() < currentFloor) {
                return true;
            }
        }
        return false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public synchronized int getCurrentFloor() { return currentFloor; }
    public synchronized void setCurrentFloor(int floor) { this.currentFloor = floor; }
    public synchronized Direction getCurrentDirection() { return currentDirection; }
    public synchronized void setCurrentDirection(Direction dir) { this.currentDirection = dir; }
    public synchronized void setState(ElevatorState state) { this.state = state; }
    public synchronized List<Request> getPendingRequests() { return pendingRequests; }
    public synchronized boolean hasRequests() { return !pendingRequests.isEmpty(); }
}

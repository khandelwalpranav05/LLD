package com.elevator_v2.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Elevator with SCAN algorithm (from HelloInterview)
 * - Continues in one direction until no more stops
 * - Then reverses
 * - Uses step() for deterministic simulation (no threading)
 */
public class Elevator {
    private final int id;
    private int currentFloor;
    private Direction direction;
    private final Set<Request> requests;
    private static final int MIN_FLOOR = 0;
    private static final int MAX_FLOOR = 10;

    public Elevator(int id) {
        this.id = id;
        this.currentFloor = 0;
        this.direction = Direction.IDLE;
        this.requests = new HashSet<>();
    }

    /**
     * Add a stop request to this elevator
     */
    public boolean addRequest(int floor, RequestType type) {
        if (floor < MIN_FLOOR || floor > MAX_FLOOR) {
            return false;
        }
        if (floor == currentFloor) {
            System.out.println("Elevator " + id + ": Already at floor " + floor);
            return true;
        }
        Request request = new Request(floor, type);
        requests.add(request);
        System.out.println("Elevator " + id + ": Added " + request);
        return true;
    }

    /**
     * SCAN Algorithm - One simulation step
     * This is the core logic from HelloInterview
     */
    public void step() {
        // Case 1: No requests - go idle
        if (requests.isEmpty()) {
            direction = Direction.IDLE;
            return;
        }

        // Case 2: IDLE with requests - pick direction to nearest
        if (direction == Direction.IDLE) {
            Request nearest = findNearestRequest();
            direction = (nearest.getFloor() > currentFloor) ? Direction.UP : Direction.DOWN;
        }

        // Case 3: Check if we should stop at current floor
        if (shouldStopAtCurrentFloor()) {
            stopAtCurrentFloor();
            
            // After stopping, check if we should reverse or go idle
            if (requests.isEmpty()) {
                direction = Direction.IDLE;
                return;
            }
            if (!hasRequestsInDirection(direction)) {
                direction = reverse(direction);
            }
            return; // Don't move this tick - we stopped
        }

        // Case 4: Reverse if no requests ahead
        if (!hasRequestsInDirection(direction)) {
            direction = reverse(direction);
        }

        // Case 5: Move one floor
        move();
    }

    private Request findNearestRequest() {
        Request nearest = null;
        int minDistance = Integer.MAX_VALUE;
        for (Request req : requests) {
            int distance = Math.abs(req.getFloor() - currentFloor);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = req;
            }
        }
        return nearest;
    }

    private boolean shouldStopAtCurrentFloor() {
        // Stop for pickups matching our direction + all destinations
        RequestType pickupType = (direction == Direction.UP) ? RequestType.PICKUP_UP : RequestType.PICKUP_DOWN;
        return requests.contains(new Request(currentFloor, pickupType)) ||
               requests.contains(new Request(currentFloor, RequestType.DESTINATION));
    }

    private void stopAtCurrentFloor() {
        System.out.println("Elevator " + id + ": STOPPED at floor " + currentFloor);
        
        // Remove matching requests
        RequestType pickupType = (direction == Direction.UP) ? RequestType.PICKUP_UP : RequestType.PICKUP_DOWN;
        requests.remove(new Request(currentFloor, pickupType));
        requests.remove(new Request(currentFloor, RequestType.DESTINATION));
    }

    private boolean hasRequestsInDirection(Direction dir) {
        for (Request req : requests) {
            if (dir == Direction.UP && req.getFloor() > currentFloor) {
                if (req.getType() == RequestType.PICKUP_UP || req.getType() == RequestType.DESTINATION) {
                    return true;
                }
            }
            if (dir == Direction.DOWN && req.getFloor() < currentFloor) {
                if (req.getType() == RequestType.PICKUP_DOWN || req.getType() == RequestType.DESTINATION) {
                    return true;
                }
            }
        }
        return false;
    }

    private Direction reverse(Direction dir) {
        return (dir == Direction.UP) ? Direction.DOWN : Direction.UP;
    }

    private void move() {
        if (direction == Direction.UP) {
            currentFloor++;
            System.out.println("Elevator " + id + ": Moving UP to " + currentFloor);
        } else if (direction == Direction.DOWN) {
            currentFloor--;
            System.out.println("Elevator " + id + ": Moving DOWN to " + currentFloor);
        }
    }

    // Getters for strategy to use
    public int getId() { return id; }
    public int getCurrentFloor() { return currentFloor; }
    public Direction getDirection() { return direction; }
    public Set<Request> getRequests() { return requests; }
    
    public boolean hasRequestsAtOrBeyond(int floor, Direction dir) {
        for (Request req : requests) {
            if (dir == Direction.UP && req.getFloor() >= floor) return true;
            if (dir == Direction.DOWN && req.getFloor() <= floor) return true;
        }
        return false;
    }
}

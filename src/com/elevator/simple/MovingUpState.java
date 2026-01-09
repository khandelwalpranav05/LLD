package com.elevator.simple;

/**
 * MOVING UP: Elevator goes up floor by floor.
 * Stops to pickup (same direction) and dropoff.
 * Reverses when no more destinations above.
 */
public class MovingUpState implements ElevatorState {
    @Override
    public void move(Elevator e) {
        // Move up one floor
        e.floor++;
        System.out.println("E" + e.id + " at floor " + e.floor);
        
        // Stop if needed
        if (e.shouldStop(e.floor)) {
            e.openDoors();
        }
        
        // Decide what's next
        if (e.hasAbove()) {
            // Keep going up
        } else if (e.hasBelow()) {
            // Reverse direction
            System.out.println("E" + e.id + " reversing to DOWN");
            e.direction = Direction.DOWN;
            e.state = new MovingDownState();
        } else if (e.requests.isEmpty()) {
            // All done
            System.out.println("E" + e.id + " going IDLE");
            e.direction = Direction.IDLE;
            e.state = new IdleState();
        }
    }
}


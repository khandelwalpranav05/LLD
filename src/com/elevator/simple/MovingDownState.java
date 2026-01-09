package com.elevator.simple;

/**
 * MOVING DOWN: Mirror of MovingUpState
 */
public class MovingDownState implements ElevatorState {
    @Override
    public void move(Elevator e) {
        // Move down one floor
        e.floor--;
        System.out.println("E" + e.id + " at floor " + e.floor);
        
        // Stop if needed
        if (e.shouldStop(e.floor)) {
            e.openDoors();
        }
        
        // Decide what's next
        if (e.hasBelow()) {
            // Keep going down
        } else if (e.hasAbove()) {
            // Reverse direction
            System.out.println("E" + e.id + " reversing to UP");
            e.direction = Direction.UP;
            e.state = new MovingUpState();
        } else if (e.requests.isEmpty()) {
            // All done
            System.out.println("E" + e.id + " going IDLE");
            e.direction = Direction.IDLE;
            e.state = new IdleState();
        }
    }
}


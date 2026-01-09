package com.elevator.simple;

/**
 * IDLE: Elevator is waiting. Decides which direction to go.
 */
public class IdleState implements ElevatorState {
    @Override
    public void move(Elevator e) {
        if (e.requests.isEmpty()) return;
        
        // Handle anyone at current floor first
        if (e.shouldStop(e.floor)) {
            e.openDoors();
        }
        
        // Decide direction based on where requests are
        if (e.hasAbove()) {
            System.out.println("E" + e.id + " going UP");
            e.direction = Direction.UP;
            e.state = new MovingUpState();
        } else if (e.hasBelow()) {
            System.out.println("E" + e.id + " going DOWN");
            e.direction = Direction.DOWN;
            e.state = new MovingDownState();
        }
    }
}


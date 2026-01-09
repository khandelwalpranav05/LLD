package com.elevator.state;

import com.elevator.model.Direction;
import com.elevator.model.Elevator;
import com.elevator.model.Request;

public class IdleState implements ElevatorState {
    @Override
    public void move(Elevator elevator) {
        if (!elevator.hasRequests()) {
            return;  // Stay idle
        }

        // First, check if anyone at current floor needs pickup
        if (elevator.shouldStopAt(elevator.getCurrentFloor())) {
            elevator.openDoors();
        }

        // Decide direction based on pending requests
        // LOOK algorithm: continue in same direction if possible
        if (elevator.hasDestinationsAbove()) {
            System.out.println("Elevator " + elevator.getId() + " starting UP from floor " + elevator.getCurrentFloor());
            elevator.setCurrentDirection(Direction.UP);
            elevator.setState(new MovingUpState());
        } else if (elevator.hasDestinationsBelow()) {
            System.out.println("Elevator " + elevator.getId() + " starting DOWN from floor " + elevator.getCurrentFloor());
            elevator.setCurrentDirection(Direction.DOWN);
            elevator.setState(new MovingDownState());
        } else {
            // All requests are at current floor and handled
            elevator.setCurrentDirection(Direction.IDLE);
        }
    }

    @Override
    public void stop(Elevator elevator) {
        System.out.println("Elevator " + elevator.getId() + " is already stopped.");
    }
}

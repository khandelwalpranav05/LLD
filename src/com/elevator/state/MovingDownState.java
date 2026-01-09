package com.elevator.state;

import com.elevator.model.Direction;
import com.elevator.model.Elevator;

public class MovingDownState implements ElevatorState {
    @Override
    public void move(Elevator elevator) {
        int current = elevator.getCurrentFloor();
        int next = current - 1;
        
        System.out.println("Elevator " + elevator.getId() + " moving DOWN: floor " + current + " → " + next);
        elevator.setCurrentFloor(next);
        
        // Check if we need to stop at this floor
        if (elevator.shouldStopAt(next)) {
            elevator.openDoors();  // Handles pickup and dropoff
        }
        
        // Decide what to do next
        if (elevator.hasDestinationsBelow()) {
            // Keep going down
            return;
        } else if (elevator.hasDestinationsAbove()) {
            // Reverse direction
            System.out.println("Elevator " + elevator.getId() + " reversing direction at floor " + next);
            elevator.setCurrentDirection(Direction.UP);
            elevator.setState(new MovingUpState());
        } else if (!elevator.hasRequests()) {
            // All done
            System.out.println("Elevator " + elevator.getId() + " all requests served, going idle at floor " + next);
            elevator.setCurrentDirection(Direction.IDLE);
            elevator.setState(new IdleState());
        }
    }

    @Override
    public void stop(Elevator elevator) {
        elevator.openDoors();
    }
}

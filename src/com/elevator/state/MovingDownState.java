package com.elevator.state;

import com.elevator.model.Elevator;

public class MovingDownState implements ElevatorState {
    @Override
    public void move(Elevator elevator) {
        int current = elevator.getCurrentFloor();
        int next = current - 1;
        System.out.println("Elevator " + elevator.getId() + " moving DOWN to " + next);
        elevator.setCurrentFloor(next);
        
        // Check if we need to stop
        if (elevator.hasRequestAt(next)) {
            elevator.setState(new IdleState());
            elevator.stop(); // Open doors
        }
    }

    @Override
    public void stop(Elevator elevator) {
        System.out.println("Elevator " + elevator.getId() + " stopping at " + elevator.getCurrentFloor());
        // Remove request logic would be here
        elevator.getPendingRequests().removeIf(r -> r.getDestinationFloor() == elevator.getCurrentFloor() || r.getSourceFloor() == elevator.getCurrentFloor());
    }
}

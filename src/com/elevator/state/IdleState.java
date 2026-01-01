package com.elevator.state;

import com.elevator.model.Direction;
import com.elevator.model.Elevator;
import com.elevator.model.Request;

public class IdleState implements ElevatorState {
    @Override
    public void move(Elevator elevator) {
        if (elevator.getPendingRequests().isEmpty()) {
            System.out.println("Elevator " + elevator.getId() + " is Idle. No requests.");
            return;
        }

        Request nextReq = elevator.getPendingRequests().get(0);
        if (nextReq.getSourceFloor() > elevator.getCurrentFloor()) {
            System.out.println("Elevator " + elevator.getId() + " starting UP.");
            elevator.setState(new MovingUpState());
        } else if (nextReq.getSourceFloor() < elevator.getCurrentFloor()) {
            System.out.println("Elevator " + elevator.getId() + " starting DOWN.");
            elevator.setState(new MovingDownState());
        } else {
            System.out.println("Elevator " + elevator.getId() + " opening doors at current floor.");
            elevator.getPendingRequests().remove(0); // Served
        }
    }

    @Override
    public void stop(Elevator elevator) {
        System.out.println("Elevator " + elevator.getId() + " is already stopped.");
    }
}

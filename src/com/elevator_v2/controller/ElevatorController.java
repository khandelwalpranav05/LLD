package com.elevator_v2.controller;

import com.elevator_v2.model.Direction;
import com.elevator_v2.model.Elevator;
import com.elevator_v2.model.RequestType;
import com.elevator_v2.strategy.DirectionAwareStrategy;
import com.elevator_v2.strategy.DispatchStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * ElevatorController - Manages multiple elevators
 * Uses Strategy pattern for dispatch (your code)
 * Uses step() simulation model (HelloInterview)
 */
public class ElevatorController {
    private final List<Elevator> elevators;
    private final DispatchStrategy strategy;
    private static final int MAX_FLOOR = 10;

    public ElevatorController(int numElevators) {
        this.elevators = new ArrayList<>();
        for (int i = 0; i < numElevators; i++) {
            elevators.add(new Elevator(i + 1));
        }
        this.strategy = new DirectionAwareStrategy();  // Can swap strategies!
    }

    /**
     * Hall call - User presses UP/DOWN button outside elevator
     */
    public void requestElevator(int floor, Direction direction) {
        if (floor < 0 || floor > MAX_FLOOR) {
            System.out.println("Invalid floor: " + floor);
            return;
        }
        if (direction == Direction.IDLE) {
            System.out.println("Invalid direction for hall call");
            return;
        }

        // Select best elevator
        Elevator selected = strategy.selectElevator(elevators, floor, direction);
        
        // Convert direction to RequestType
        RequestType type = (direction == Direction.UP) ? RequestType.PICKUP_UP : RequestType.PICKUP_DOWN;
        selected.addRequest(floor, type);
    }

    /**
     * Car call - User inside elevator presses destination floor
     */
    public void selectFloor(int elevatorId, int floor) {
        Elevator elevator = elevators.stream()
            .filter(e -> e.getId() == elevatorId)
            .findFirst()
            .orElse(null);
        
        if (elevator == null) {
            System.out.println("Invalid elevator ID: " + elevatorId);
            return;
        }
        
        elevator.addRequest(floor, RequestType.DESTINATION);
    }

    /**
     * Simulation step - moves all elevators one tick
     */
    public void step() {
        for (Elevator elevator : elevators) {
            elevator.step();
        }
    }

    public List<Elevator> getElevators() {
        return elevators;
    }
}

package com.elevator.controller;

import com.elevator.model.Elevator;
import com.elevator.model.Request;
import com.elevator.strategy.DispatchStrategy;
import com.elevator.strategy.FCFSStrategy;

import java.util.ArrayList;
import java.util.List;

public class ElevatorController {
    private static ElevatorController instance;
    private List<Elevator> elevators;
    private DispatchStrategy strategy;

    private ElevatorController(int numElevators) {
        this.elevators = new ArrayList<>();
        for (int i = 0; i < numElevators; i++) {
            Elevator e = new Elevator(i + 1);
            elevators.add(e);
            new Thread(e).start(); // Start the Active Object thread
        }
        this.strategy = new FCFSStrategy(); // Default Strategy
    }

    public static synchronized ElevatorController getInstance(int numElevators) {
        if (instance == null) {
            instance = new ElevatorController(numElevators);
        }
        return instance;
    }

    public void requestElevator(int source, int destination) {
        Request req = new Request(source, destination);
        Elevator selected = strategy.selectElevator(elevators, req);
        selected.addRequest(req);
    }
}

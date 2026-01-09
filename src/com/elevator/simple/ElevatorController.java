package com.elevator.simple;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * CONTROLLER: Central coordinator.
 * Receives requests, picks best elevator, dispatches.
 */
public class ElevatorController {
    private static ElevatorController instance;
    private List<Elevator> elevators = new ArrayList<>();
    
    private ElevatorController(int count) {
        for (int i = 0; i < count; i++) {
            Elevator e = new Elevator(i + 1);
            elevators.add(e);
            new Thread(e).start();  // Start elevator thread
        }
    }
    
    public static synchronized ElevatorController getInstance(int count) {
        if (instance == null) {
            instance = new ElevatorController(count);
        }
        return instance;
    }
    
    // Reset for testing
    public static synchronized void reset() {
        instance = null;
    }
    
    /**
     * Handle request: Create Request, pick elevator, dispatch
     */
    public void request(int source, int destination) {
        Request req = new Request(source, destination);
        Elevator best = selectNearestElevator(req);
        best.addRequest(req);
    }
    
    /**
     * Simple Strategy: Pick nearest elevator
     */
    private Elevator selectNearestElevator(Request req) {
        return elevators.stream()
            .min(Comparator.comparingInt(e -> Math.abs(e.floor - req.source)))
            .orElse(elevators.get(0));
    }
}


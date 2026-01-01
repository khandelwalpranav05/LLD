package com.elevator.strategy;

import com.elevator.model.Elevator;
import com.elevator.model.Request;
import java.util.List;

public class FCFSStrategy implements DispatchStrategy {
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        // Simple Logic: Pick the first available elevator (or random)
        // In a real FCFS, we'd pick the one with the shortest queue.
        // For simplicity, we just pick the first one.
        return elevators.get(0);
    }
}

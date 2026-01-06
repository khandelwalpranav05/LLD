package com.elevator_v2.strategy;

import com.elevator_v2.model.Direction;
import com.elevator_v2.model.Elevator;
import java.util.List;

/**
 * Strategy Pattern (from your original code) - Kept for extensibility!
 */
public interface DispatchStrategy {
    Elevator selectElevator(List<Elevator> elevators, int floor, Direction requestDirection);
}

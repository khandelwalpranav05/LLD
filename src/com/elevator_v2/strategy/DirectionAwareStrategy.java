package com.elevator_v2.strategy;

import com.elevator_v2.model.Direction;
import com.elevator_v2.model.Elevator;
import java.util.List;

/**
 * Direction-Aware Strategy (from HelloInterview - "Great Solution")
 * 
 * Priority:
 * 1. IDLE elevators (prefer nearest)
 * 2. Elevators going in same direction that haven't passed the floor
 * 3. Fallback: any elevator (nearest)
 */
public class DirectionAwareStrategy implements DispatchStrategy {
    
    @Override
    public Elevator selectElevator(List<Elevator> elevators, int floor, Direction requestDirection) {
        Elevator best = null;
        int bestScore = Integer.MAX_VALUE;
        
        for (Elevator elevator : elevators) {
            int score = calculateScore(elevator, floor, requestDirection);
            if (score < bestScore) {
                bestScore = score;
                best = elevator;
            }
        }
        
        return best;
    }
    
    private int calculateScore(Elevator elevator, int floor, Direction requestDirection) {
        int distance = Math.abs(elevator.getCurrentFloor() - floor);
        Direction elevatorDir = elevator.getDirection();
        
        // IDLE elevator - great candidate
        if (elevatorDir == Direction.IDLE) {
            return distance;  // Lower is better
        }
        
        // Going same direction and hasn't passed the floor
        if (elevatorDir == requestDirection) {
            if (elevatorDir == Direction.UP && elevator.getCurrentFloor() <= floor) {
                return distance;  // On the way
            }
            if (elevatorDir == Direction.DOWN && elevator.getCurrentFloor() >= floor) {
                return distance;  // On the way
            }
        }
        
        // Elevator going opposite direction or already passed - add penalty
        return distance + 1000;  // Large penalty
    }
}

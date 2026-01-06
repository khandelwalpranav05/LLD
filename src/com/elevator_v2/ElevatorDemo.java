package com.elevator_v2;

import com.elevator_v2.controller.ElevatorController;
import com.elevator_v2.model.Direction;

/**
 * Demo showing the hybrid elevator system
 * 
 * Best of both worlds:
 * - Strategy Pattern (your code) for extensible dispatch
 * - SCAN Algorithm (HelloInterview) for efficient movement
 * - step() simulation (HelloInterview) for testability
 * - Request types (HelloInterview) for realistic hall/car calls
 */
public class ElevatorDemo {
    public static void main(String[] args) {
        System.out.println("=== Hybrid Elevator System Demo ===\n");
        
        // Create system with 2 elevators
        ElevatorController controller = new ElevatorController(2);
        
        // Scenario: Multiple hall calls
        System.out.println("--- Hall Calls ---");
        controller.requestElevator(3, Direction.UP);   // Someone at floor 3 wants to go up
        controller.requestElevator(5, Direction.DOWN); // Someone at floor 5 wants to go down
        controller.requestElevator(2, Direction.UP);   // Someone at floor 2 wants to go up
        
        // Simulate elevator movement
        System.out.println("\n--- Simulation ---");
        for (int i = 0; i < 10; i++) {
            System.out.println("\n[Tick " + (i + 1) + "]");
            controller.step();
            
            // User enters elevator 1 at floor 2, selects floor 7
            if (i == 2) {
                System.out.println("  > User enters Elevator 1, selects floor 7");
                controller.selectFloor(1, 7);
            }
        }
        
        System.out.println("\n=== Demo Complete ===");
    }
}

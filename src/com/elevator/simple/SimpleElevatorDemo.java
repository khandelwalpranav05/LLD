package com.elevator.simple;

/**
 * TEST: Verifies the scenario 0 → 3 → 6 → 8 → 5 → 0
 * 
 * Requests:
 * 1. 0 → 6 (ground to 6th)
 * 2. 3 → 8 (3rd to 8th, going UP)
 * 3. 5 → 0 (5th to ground, going DOWN)
 * 
 * Expected: Elevator goes up, picks up along the way,
 * then reverses and comes back down.
 */
public class SimpleElevatorDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("=== ELEVATOR SYSTEM TEST ===\n");
        
        ElevatorController controller = ElevatorController.getInstance(1);
        Thread.sleep(300);
        
        System.out.println(">>> Request: 0 → 6");
        controller.request(0, 6);
        Thread.sleep(100);
        
        System.out.println(">>> Request: 3 → 8");
        controller.request(3, 8);
        Thread.sleep(100);
        
        System.out.println(">>> Request: 5 → 0");
        controller.request(5, 0);
        
        // Let it run
        Thread.sleep(12000);
        
        System.out.println("\n=== TEST COMPLETE ===");
        System.exit(0);
    }
}


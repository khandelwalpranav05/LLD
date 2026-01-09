package com.elevator;

import com.elevator.controller.ElevatorController;

/**
 * Test Scenario:
 * 1. Request 1: 0 → 6 (someone at ground floor going to 6)
 * 2. Request 2: 3 → 8 (someone at floor 3 going to 8)  
 * 3. Request 3: 5 → 0 (someone at floor 5 going DOWN to 0)
 * 
 * Expected OPTIMAL flow with LOOK algorithm:
 * 0 → 3 (pickup) → 5 (pickup for DOWN later) → 6 (dropoff) → 8 (dropoff) → 5 → 0 (dropoff)
 * 
 * But actually: Since passenger at floor 5 is going DOWN, and we're going UP,
 * we DON'T pick them up on the way up. We pick them up when coming back down.
 * 
 * So the flow is:
 * 0 (pickup) → 3 (pickup) → 6 (dropoff) → 8 (dropoff) → 5 (pickup) → 0 (dropoff)
 */
public class ElevatorDemo {
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║           ELEVATOR SYSTEM - LOOK ALGORITHM TEST           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        // Use only 1 elevator for clearer demonstration
        ElevatorController controller = ElevatorController.getInstance(1);

        // Give elevator time to initialize
        try { Thread.sleep(500); } catch (Exception e) {}

        // Simulate requests coming in
        new Thread(() -> {
            try {
                System.out.println("\n>>> Request 1: Floor 0 → 6");
                controller.requestElevator(0, 6);
                
                Thread.sleep(200);
                
                System.out.println("\n>>> Request 2: Floor 3 → 8");
                controller.requestElevator(3, 8);
                
                Thread.sleep(200);
                
                System.out.println("\n>>> Request 3: Floor 5 → 0 (going DOWN)");
                controller.requestElevator(5, 0);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Let simulation run
        try {
            Thread.sleep(15000);
        } catch (Exception e) {}
        
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                    SIMULATION COMPLETE                     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        
        System.exit(0);
    }
}

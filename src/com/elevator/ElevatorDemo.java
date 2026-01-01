package com.elevator;

import com.elevator.controller.ElevatorController;

public class ElevatorDemo {
    public static void main(String[] args) {
        // 1. Setup System with 2 Elevators
        ElevatorController controller = ElevatorController.getInstance(2);

        // 2. Thread 1: User Requests 6, 2, 4 (Mixed Order)
        new Thread(() -> {
            try { Thread.sleep(100); } catch (Exception e) {}
            System.out.println("Requesting 0->6");
            controller.requestElevator(0, 6);
            
            try { Thread.sleep(50); } catch (Exception e) {}
            System.out.println("Requesting 0->2");
            controller.requestElevator(0, 2);

            try { Thread.sleep(50); } catch (Exception e) {}
            System.out.println("Requesting 0->4");
            controller.requestElevator(0, 4);
        }).start();

        // 4. Simulation Loop (No longer needed, threads are running)
        System.out.println("\n--- Simulation Start ---");
        try {
            // Keep main thread alive to let elevators run
            Thread.sleep(10000); 
        } catch (Exception e) {}
    }
}

package com.parkinglot;

import com.parkinglot.enums.*;
import com.parkinglot.model.*;
import com.parkinglot.service.ParkingLot;
import com.parkinglot.strategy.NearestFirstStrategy;
import com.parkinglot.strategy.HourlyPricingStrategy;

public class ParkingLotDemo {
    public static void main(String[] args) {
        System.out.println("=== Initializing Parking Lot System ===\n");
        
        // Reset for clean demo (useful for testing)
        ParkingLot.resetInstance();
        
        // 1. Initialize System
        ParkingLot parkingLot = ParkingLot.getInstance();
        parkingLot.setParkingStrategy(new NearestFirstStrategy());
        
        // 2. Create Floors and Spots
        ParkingFloor floor1 = new ParkingFloor("Floor-1");
        floor1.addSpot(new CompactSpot("C1"));
        floor1.addSpot(new CompactSpot("C2"));
        floor1.addSpot(new LargeSpot("L1"));
        
        ParkingFloor floor2 = new ParkingFloor("Floor-2");
        floor2.addSpot(new CompactSpot("C3"));
        
        parkingLot.addFloor(floor1);
        parkingLot.addFloor(floor2);
        
        // 3. Create Entry and Exit Gates
        EntryGate entryGate1 = new EntryGate("ENTRY-1");
        EntryGate entryGate2 = new EntryGate("ENTRY-2");
        ExitGate exitGate1 = new ExitGate("EXIT-1", new HourlyPricingStrategy());
        ExitGate exitGate2 = new ExitGate("EXIT-2", new HourlyPricingStrategy());
        
        parkingLot.addEntryGate(entryGate1);
        parkingLot.addEntryGate(entryGate2);
        parkingLot.addExitGate(exitGate1);
        parkingLot.addExitGate(exitGate2);
        
        System.out.println("Parking Lot initialized:");
        System.out.println("  - 2 floors (Floor-1, Floor-2)");
        System.out.println("  - 2 entry gates (ENTRY-1, ENTRY-2)");
        System.out.println("  - 2 exit gates (EXIT-1, EXIT-2)");
        
        // 4. Simulate Vehicles Entering via Different Gates
        System.out.println("\n=== Entry Process ===\n");
        
        Vehicle car1 = new Car("KA-01-1234");
        Vehicle truck1 = new Truck("KA-01-9999");
        Vehicle car2 = new Car("KA-01-5678");
        
        // Car 1 enters via Entry Gate 1
        System.out.println("Car1 approaching ENTRY-1...");
        Ticket t1 = entryGate1.generateTicket(car1);
        
        // Truck enters via Entry Gate 2
        System.out.println("\nTruck approaching ENTRY-2...");
        Ticket t2 = entryGate2.generateTicket(truck1);
        
        // Car 2 enters via Entry Gate 1
        System.out.println("\nCar2 approaching ENTRY-1...");
        Ticket t3 = entryGate1.generateTicket(car2);
        
        // 5. Simulate Full Scenario
        System.out.println("\n=== Capacity Test ===\n");
        
        Vehicle car3 = new Car("KA-01-FULL");
        System.out.println("Car3 approaching ENTRY-2...");
        Ticket t4 = entryGate2.generateTicket(car3); // Should be on Floor 2
        
        Vehicle car4 = new Car("KA-01-OVERFLOW");
        System.out.println("\nCar4 approaching ENTRY-1 (all compact spots taken)...");
        Ticket t5 = entryGate1.generateTicket(car4); // Should fail
        
        // 6. Simulate Exit via Exit Gates
        System.out.println("\n=== Exit Process ===\n");
        
        if (t1 != null) {
            System.out.println("Car1 (KA-01-1234) approaching EXIT-1...");
            System.out.println("  Entry Gate was: " + t1.getEntryGateId());
            
            ExitGate.ExitResult result = exitGate1.processExit(t1);
            System.out.println("  " + result.getMessage());
        }
        
        if (t2 != null) {
            System.out.println("\nTruck (KA-01-9999) approaching EXIT-2...");
            System.out.println("  Entry Gate was: " + t2.getEntryGateId());
            
            ExitGate.ExitResult result = exitGate2.processExit(t2);
            System.out.println("  " + result.getMessage());
        }
        
        // 7. Show that trying to use a paid ticket fails
        System.out.println("\n=== Edge Case: Reusing Paid Ticket ===\n");
        if (t1 != null) {
            System.out.println("Trying to reuse t1 at EXIT-2...");
            ExitGate.ExitResult result = exitGate2.processExit(t1);
            System.out.println("  " + result.getMessage());
        }
        
        System.out.println("\n=== Demo Complete ===");
    }
}

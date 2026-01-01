package com.parkinglot;

import com.parkinglot.enums.*;
import com.parkinglot.model.*;
import com.parkinglot.service.ParkingLot;
import com.parkinglot.strategy.NearestFirstStrategy;
import com.parkinglot.strategy.HourlyPricingStrategy;
import com.parkinglot.strategy.PricingStrategy;

public class ParkingLotDemo {
    public static void main(String[] args) {
        System.out.println("Initializing Parking Lot System...");
        
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
        
        System.out.println("Parking Lot initialized with 2 floors.");
        
        // 3. Simulate Entry
        Vehicle car1 = new Car("KA-01-1234");
        Vehicle truck1 = new Truck("KA-01-9999");
        Vehicle car2 = new Car("KA-01-5678");
        
        System.out.println("\n--- Entry Process ---");
        Ticket t1 = parkingLot.getTicket(car1);
        Ticket t2 = parkingLot.getTicket(truck1);
        Ticket t3 = parkingLot.getTicket(car2);
        
        // 4. Simulate Full Scenario
        Vehicle car3 = new Car("KA-01-FULL");
        Ticket t4 = parkingLot.getTicket(car3); // Should be on Floor 2
        
        Vehicle car4 = new Car("KA-01-OVERFLOW");
        Ticket t5 = parkingLot.getTicket(car4); // Should fail if C1, C2, C3 are taken
        
        // 5. Simulate Exit
        System.out.println("\n--- Exit Process ---");
        if (t1 != null) {
            PricingStrategy pricingStrategy = new HourlyPricingStrategy();
            double price = pricingStrategy.calculatePrice(t1);
            System.out.println("Vehicle " + car1.getLicensePlate() + " exiting. Price: $" + price);
            t1.markPaid(price, System.currentTimeMillis());
            
            // Return spot
            t1.getAssignedSpot().removeVehicle();
            floor1.returnSpot(t1.getAssignedSpot());
            System.out.println("Spot " + t1.getAssignedSpot().getId() + " is now free.");
        }
    }
}

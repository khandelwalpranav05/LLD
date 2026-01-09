package com.parkinglot.simple;

/**
 * Demo for Simplified Parking Lot
 */
public class SimpleParkingDemo {
    
    public static void main(String[] args) {
        System.out.println("=== SIMPLE PARKING LOT DEMO ===\n");
        
        // 1. Setup
        ParkingLot.resetInstance();
        ParkingLot lot = ParkingLot.getInstance();
        
        // Add spots
        lot.addSpot(new ParkingSpot("S1", SpotType.SMALL));
        lot.addSpot(new ParkingSpot("S2", SpotType.SMALL));
        lot.addSpot(new ParkingSpot("M1", SpotType.MEDIUM));
        lot.addSpot(new ParkingSpot("M2", SpotType.MEDIUM));
        lot.addSpot(new ParkingSpot("M3", SpotType.MEDIUM));
        lot.addSpot(new ParkingSpot("L1", SpotType.LARGE));
        
        System.out.println("Total spots: " + lot.getTotalSpots());
        System.out.println("Available MEDIUM spots: " + lot.getAvailableSpots(SpotType.MEDIUM));
        
        // 2. Park vehicles
        System.out.println("\n--- Parking Vehicles ---");
        
        Vehicle bike1 = new Vehicle("BIKE-001", VehicleType.BIKE);
        Vehicle car1 = new Vehicle("CAR-001", VehicleType.CAR);
        Vehicle car2 = new Vehicle("CAR-002", VehicleType.CAR);
        Vehicle truck1 = new Vehicle("TRUCK-001", VehicleType.TRUCK);
        
        Ticket t1 = lot.parkVehicle(bike1);
        Ticket t2 = lot.parkVehicle(car1);
        Ticket t3 = lot.parkVehicle(car2);
        Ticket t4 = lot.parkVehicle(truck1);
        
        System.out.println("\nAvailable MEDIUM spots after parking: " + 
                          lot.getAvailableSpots(SpotType.MEDIUM));
        
        // 3. Try to park when full
        System.out.println("\n--- Capacity Test ---");
        
        Vehicle truck2 = new Vehicle("TRUCK-002", VehicleType.TRUCK);
        Ticket t5 = lot.parkVehicle(truck2);  // Should fail - only 1 large spot
        
        // 4. Unpark vehicles
        System.out.println("\n--- Unparking Vehicles ---");
        
        if (t2 != null) {
            double fee = lot.unparkVehicle(t2);
            System.out.println("Car1 paid: $" + fee);
        }
        
        if (t4 != null) {
            double fee = lot.unparkVehicle(t4);
            System.out.println("Truck1 paid: $" + fee);
        }
        
        // 5. Now truck2 can park
        System.out.println("\n--- After Unpark ---");
        t5 = lot.parkVehicle(truck2);  // Should succeed now
        
        // 6. Edge case: try to use same ticket again
        System.out.println("\n--- Edge Case: Reuse Ticket ---");
        if (t2 != null) {
            double fee = lot.unparkVehicle(t2);  // Should fail
        }
        
        System.out.println("\n=== DEMO COMPLETE ===");
    }
}



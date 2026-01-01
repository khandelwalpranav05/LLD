package com.parkinglot.service;

import com.parkinglot.model.*;
import com.parkinglot.enums.*;
import com.parkinglot.strategy.ParkingAssignmentStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Singleton ParkingLot System.
 * Acts as the Facade for the entire system.
 */
public class ParkingLot {
    private static ParkingLot instance;
    private List<ParkingFloor> floors;
    private List<EntryGate> entryGates;
    private List<ExitGate> exitGates;
    
    private ParkingAssignmentStrategy parkingStrategy;

    private ParkingLot() {
        this.floors = new ArrayList<>();
        this.entryGates = new ArrayList<>();
        this.exitGates = new ArrayList<>();
    }

    public static synchronized ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }
    
    public void setParkingStrategy(ParkingAssignmentStrategy strategy) {
        this.parkingStrategy = strategy;
    }

    public Ticket getTicket(Vehicle vehicle) {
        // 1. Find a spot using the Strategy
        ParkingSpot spot = parkingStrategy.findSpot(floors, vehicle.getType());
        
        if (spot == null) {
            System.out.println("Parking Full for vehicle: " + vehicle.getType());
            return null;
        }

        // 2. Assign vehicle to spot (Thread-safe)
        // Note: The strategy might return a spot, but another thread might grab it.
        // In a real DB, we would lock. Here, our ParkingFloor.getSpot() already removed it from the queue,
        // so it is reserved for us.
        
        boolean success = spot.assignVehicle(vehicle);
        if (!success) {
            // Should not happen if ParkingFloor.getSpot() works correctly
            System.out.println("Concurrency Error: Spot already taken.");
            return null;
        }

        // 3. Generate Ticket
        String ticketId = UUID.randomUUID().toString();
        Ticket ticket = new Ticket(ticketId, spot, vehicle.getLicensePlate());
        System.out.println("Ticket generated: " + ticketId + " for " + vehicle.getLicensePlate() + " at spot " + spot.getId());
        return ticket;
    }
    
    public List<ParkingFloor> getFloors() {
        return floors;
    }
}

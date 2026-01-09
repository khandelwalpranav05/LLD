package com.parkinglot.service;

import com.parkinglot.model.*;
import com.parkinglot.enums.*;
import com.parkinglot.strategy.ParkingAssignmentStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    
    // Track which floor each spot belongs to (for returning spots)
    private Map<String, ParkingFloor> spotToFloorMap;

    private ParkingLot() {
        this.floors = new ArrayList<>();
        this.entryGates = new ArrayList<>();
        this.exitGates = new ArrayList<>();
        this.spotToFloorMap = new ConcurrentHashMap<>();
    }

    public static synchronized ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }
    
    // For testing - allows resetting the singleton
    public static synchronized void resetInstance() {
        instance = null;
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }
    
    public void addEntryGate(EntryGate gate) {
        entryGates.add(gate);
    }
    
    public void addExitGate(ExitGate gate) {
        exitGates.add(gate);
    }
    
    public void setParkingStrategy(ParkingAssignmentStrategy strategy) {
        this.parkingStrategy = strategy;
    }

    /**
     * Get a ticket for a vehicle entering through a specific gate.
     * 
     * @param vehicle The vehicle entering
     * @param entryGateId The ID of the entry gate used
     * @return Ticket if spot found, null if parking full
     */
    public Ticket getTicket(Vehicle vehicle, String entryGateId) {
        // 1. Find a spot using the Strategy
        ParkingSpot spot = parkingStrategy.findSpot(floors, vehicle.getType());
        
        if (spot == null) {
            System.out.println("Parking Full for vehicle: " + vehicle.getType());
            return null;
        }

        // 2. Assign vehicle to spot (Thread-safe)
        boolean success = spot.assignVehicle(vehicle);
        if (!success) {
            System.out.println("Concurrency Error: Spot already taken.");
            return null;
        }

        // 3. Generate Ticket with gate information
        String ticketId = UUID.randomUUID().toString();
        Ticket ticket = new Ticket(ticketId, spot, vehicle.getLicensePlate(), entryGateId);
        
        System.out.println("Ticket generated: " + ticketId + 
                          " for " + vehicle.getLicensePlate() + 
                          " at spot " + spot.getId() +
                          " via Gate " + entryGateId);
        return ticket;
    }
    
    /**
     * Return a spot to its floor's free queue.
     * Called by ExitGate after payment.
     */
    public void returnSpot(ParkingSpot spot) {
        // Find which floor this spot belongs to and return it
        for (ParkingFloor floor : floors) {
            floor.returnSpot(spot);
        }
    }
    
    public List<ParkingFloor> getFloors() {
        return floors;
    }
    
    public List<EntryGate> getEntryGates() {
        return entryGates;
    }
    
    public List<ExitGate> getExitGates() {
        return exitGates;
    }
}

package com.parkinglot.model;

import com.parkinglot.service.ParkingLot;

/**
 * Entry Gate - responsible for:
 * 1. Generating tickets for incoming vehicles
 * 2. Recording which gate the vehicle entered from
 * 
 * In a real system, this would also:
 * - Control the physical barrier
 * - Integrate with ANPR (Automatic Number Plate Recognition)
 * - Display spot assignment on LED board
 */
public class EntryGate extends Gate {
    
    public EntryGate(String id) {
        super(id);
    }
    
    /**
     * Generate a ticket for a vehicle entering through this gate.
     * The ticket records which gate was used for entry.
     */
    public Ticket generateTicket(Vehicle vehicle) {
        return ParkingLot.getInstance().getTicket(vehicle, this.getId());
    }
}

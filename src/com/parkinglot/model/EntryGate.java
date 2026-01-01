package com.parkinglot.model;

import com.parkinglot.service.ParkingLot;

public class EntryGate extends Gate {
    public EntryGate(String id) {
        super(id);
    }
    
    public Ticket generateTicket(Vehicle vehicle) {
        return ParkingLot.getInstance().getTicket(vehicle);
    }
}

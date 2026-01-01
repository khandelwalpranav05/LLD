package com.parkinglot.model;

import com.parkinglot.enums.TicketStatus;

public class Ticket {
    private String id;
    private long entryTime;
    private long exitTime;
    private double amount;
    private TicketStatus status;
    private ParkingSpot assignedSpot;
    private String vehicleLicensePlate;

    public Ticket(String id, ParkingSpot assignedSpot, String vehicleLicensePlate) {
        this.id = id;
        this.assignedSpot = assignedSpot;
        this.vehicleLicensePlate = vehicleLicensePlate;
        this.entryTime = System.currentTimeMillis();
        this.status = TicketStatus.ACTIVE;
    }

    public void markPaid(double amount, long exitTime) {
        this.amount = amount;
        this.exitTime = exitTime;
        this.status = TicketStatus.PAID;
    }

    public String getId() { return id; }
    public long getEntryTime() { return entryTime; }
    public ParkingSpot getAssignedSpot() { return assignedSpot; }
    public TicketStatus getStatus() { return status; }
}

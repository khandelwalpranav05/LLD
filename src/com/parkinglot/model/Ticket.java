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
    
    // Track which gates were used
    private String entryGateId;
    private String exitGateId;

    public Ticket(String id, ParkingSpot assignedSpot, String vehicleLicensePlate, String entryGateId) {
        this.id = id;
        this.assignedSpot = assignedSpot;
        this.vehicleLicensePlate = vehicleLicensePlate;
        this.entryGateId = entryGateId;
        this.entryTime = System.currentTimeMillis();
        this.status = TicketStatus.ACTIVE;
    }

    public void markPaid(double amount, long exitTime, String exitGateId) {
        this.amount = amount;
        this.exitTime = exitTime;
        this.exitGateId = exitGateId;
        this.status = TicketStatus.PAID;
    }

    public String getId() { return id; }
    public long getEntryTime() { return entryTime; }
    public long getExitTime() { return exitTime; }
    public double getAmount() { return amount; }
    public ParkingSpot getAssignedSpot() { return assignedSpot; }
    public TicketStatus getStatus() { return status; }
    public String getVehicleLicensePlate() { return vehicleLicensePlate; }
    public String getEntryGateId() { return entryGateId; }
    public String getExitGateId() { return exitGateId; }
}

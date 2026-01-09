package com.parkinglot.simple;

/**
 * Ticket - issued when vehicle parks
 */
public class Ticket {
    private String id;
    private Vehicle vehicle;
    private ParkingSpot spot;
    private long entryTime;
    
    public Ticket(String id, Vehicle vehicle, ParkingSpot spot) {
        this.id = id;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = System.currentTimeMillis();
    }
    
    public String getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
    public long getEntryTime() { return entryTime; }
    
    /**
     * Calculate parking duration in hours
     */
    public double getHoursParked() {
        long duration = System.currentTimeMillis() - entryTime;
        return Math.max(1, Math.ceil(duration / (1000.0 * 60 * 60)));
    }
}



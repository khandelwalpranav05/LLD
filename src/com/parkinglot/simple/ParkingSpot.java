package com.parkinglot.simple;

/**
 * ParkingSpot - represents a single parking spot
 * 
 * Key: synchronized methods for thread safety
 */
public class ParkingSpot {
    private String id;
    private SpotType type;
    private Vehicle currentVehicle;
    
    public ParkingSpot(String id, SpotType type) {
        this.id = id;
        this.type = type;
        this.currentVehicle = null;
    }
    
    /**
     * Try to park a vehicle in this spot
     * @return true if successful, false if spot taken
     */
    public synchronized boolean park(Vehicle vehicle) {
        if (currentVehicle != null) {
            return false;  // Already occupied
        }
        this.currentVehicle = vehicle;
        return true;
    }
    
    /**
     * Remove vehicle from spot
     */
    public synchronized void unpark() {
        this.currentVehicle = null;
    }
    
    public boolean isAvailable() {
        return currentVehicle == null;
    }
    
    public String getId() { return id; }
    public SpotType getType() { return type; }
    public Vehicle getCurrentVehicle() { return currentVehicle; }
}



package com.parkinglot.model;

import com.parkinglot.enums.SpotType;

/**
 * Represents a physical parking spot.
 * 
 * Concurrency Note:
 * Methods modifying the state of the spot (assignVehicle, removeVehicle) are synchronized.
 * This ensures that two threads (Entry Gates) cannot assign the same spot to different vehicles simultaneously.
 */
public abstract class ParkingSpot {
    private String id;
    private SpotType type;
    private boolean isFree;
    private Vehicle currentVehicle;

    public ParkingSpot(String id, SpotType type) {
        this.id = id;
        this.type = type;
        this.isFree = true;
    }

    /**
     * Thread-safe method to assign a vehicle to this spot.
     * @param vehicle The vehicle to park.
     * @return true if assignment was successful, false if spot was already taken.
     */
    public synchronized boolean assignVehicle(Vehicle vehicle) {
        if (!isFree) {
            return false;
        }
        this.currentVehicle = vehicle;
        this.isFree = false;
        return true;
    }

    public synchronized boolean removeVehicle() {
        if (isFree) {
            return false;
        }
        this.currentVehicle = null;
        this.isFree = true;
        return true;
    }

    public String getId() {
        return id;
    }

    public SpotType getType() {
        return type;
    }

    public boolean isFree() {
        return isFree;
    }
    
    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }
}

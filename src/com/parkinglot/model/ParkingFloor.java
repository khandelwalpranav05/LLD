package com.parkinglot.model;

import com.parkinglot.enums.SpotType;
import com.parkinglot.enums.VehicleType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ParkingFloor {
    private String floorId;
    // Map of SpotType to a Queue of Free Spots.
    // Using ConcurrentLinkedDeque for thread-safe non-blocking access.
    private Map<SpotType, Deque<ParkingSpot>> freeSpots;
    private Map<String, ParkingSpot> occupiedSpots;

    public ParkingFloor(String floorId) {
        this.floorId = floorId;
        this.freeSpots = new ConcurrentHashMap<>();
        this.occupiedSpots = new ConcurrentHashMap<>();
        
        // Initialize queues for each spot type
        for (SpotType type : SpotType.values()) {
            freeSpots.put(type, new ConcurrentLinkedDeque<>());
        }
    }

    public void addSpot(ParkingSpot spot) {
        freeSpots.get(spot.getType()).add(spot);
    }

    /**
     * Tries to book a spot of a specific type.
     * This is where concurrency is handled.
     */
    public ParkingSpot getSpot(SpotType type) {
        Deque<ParkingSpot> spots = freeSpots.get(type);
        if (spots == null || spots.isEmpty()) {
            return null;
        }
        
        // Poll retrieves and removes the head of the queue.
        // This is atomic for ConcurrentLinkedDeque.
        ParkingSpot spot = spots.poll();
        if (spot != null) {
            occupiedSpots.put(spot.getId(), spot);
        }
        return spot;
    }

    public void returnSpot(ParkingSpot spot) {
        if (occupiedSpots.remove(spot.getId()) != null) {
            spot.removeVehicle();
            freeSpots.get(spot.getType()).add(spot);
        }
    }

    public String getFloorId() {
        return floorId;
    }
    
    public boolean hasSpace(SpotType type) {
        return !freeSpots.get(type).isEmpty();
    }
}

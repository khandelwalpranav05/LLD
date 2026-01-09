package com.parkinglot.simple;

import java.util.*;

/**
 * ParkingLot - Main service class (Singleton)
 * 
 * Simplified for interview:
 * - Single level (no floors)
 * - Simple spot assignment (first available)
 * - Simple pricing (hourly)
 */
public class ParkingLot {
    private static ParkingLot instance;
    
    // Spots organized by type for O(1) lookup
    private Map<SpotType, List<ParkingSpot>> spotsByType;
    
    // Track active tickets by vehicle license plate
    private Map<String, Ticket> activeTickets;
    
    // Pricing per hour by vehicle type
    private Map<VehicleType, Double> hourlyRates;
    
    private int ticketCounter = 0;
    
    private ParkingLot() {
        spotsByType = new HashMap<>();
        activeTickets = new HashMap<>();
        hourlyRates = new HashMap<>();
        
        // Initialize spot lists
        for (SpotType type : SpotType.values()) {
            spotsByType.put(type, new ArrayList<>());
        }
        
        // Default pricing
        hourlyRates.put(VehicleType.BIKE, 10.0);
        hourlyRates.put(VehicleType.CAR, 20.0);
        hourlyRates.put(VehicleType.TRUCK, 30.0);
    }
    
    public static synchronized ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }
    
    // For testing
    public static void resetInstance() {
        instance = null;
    }
    
    // ==================== SETUP ====================
    
    public void addSpot(ParkingSpot spot) {
        spotsByType.get(spot.getType()).add(spot);
    }
    
    // ==================== CORE OPERATIONS ====================
    
    /**
     * Park a vehicle - find spot, create ticket
     * @return Ticket if successful, null if no spot available
     */
    public Ticket parkVehicle(Vehicle vehicle) {
        // 1. Find appropriate spot type for vehicle
        SpotType requiredType = getSpotTypeForVehicle(vehicle.getType());
        
        // 2. Find an available spot
        ParkingSpot spot = findAvailableSpot(requiredType);
        
        if (spot == null) {
            System.out.println("No spot available for " + vehicle.getType());
            return null;
        }
        
        // 3. Try to park (synchronized for thread safety)
        if (!spot.park(vehicle)) {
            // Rare: spot was taken between find and park
            System.out.println("Spot was taken, try again");
            return null;
        }
        
        // 4. Create and return ticket
        String ticketId = "T-" + (++ticketCounter);
        Ticket ticket = new Ticket(ticketId, vehicle, spot);
        activeTickets.put(vehicle.getLicensePlate(), ticket);
        
        System.out.println("Parked " + vehicle.getLicensePlate() + 
                          " at spot " + spot.getId() + 
                          ". Ticket: " + ticketId);
        return ticket;
    }
    
    /**
     * Unpark a vehicle - calculate fee, release spot
     * @return fee charged, or -1 if ticket not found
     */
    public double unparkVehicle(Ticket ticket) {
        if (ticket == null) {
            System.out.println("Invalid ticket");
            return -1;
        }
        
        // 1. Validate ticket exists
        Ticket activeTicket = activeTickets.get(ticket.getVehicle().getLicensePlate());
        if (activeTicket == null || !activeTicket.getId().equals(ticket.getId())) {
            System.out.println("Ticket not found or already used");
            return -1;
        }
        
        // 2. Calculate fee
        double hours = ticket.getHoursParked();
        double rate = hourlyRates.get(ticket.getVehicle().getType());
        double fee = hours * rate;
        
        // 3. Release spot
        ticket.getSpot().unpark();
        
        // 4. Remove from active tickets
        activeTickets.remove(ticket.getVehicle().getLicensePlate());
        
        System.out.println("Unparked " + ticket.getVehicle().getLicensePlate() + 
                          ". Duration: " + hours + " hrs. Fee: $" + fee);
        return fee;
    }
    
    // ==================== HELPERS ====================
    
    /**
     * Map vehicle type to appropriate spot type
     */
    private SpotType getSpotTypeForVehicle(VehicleType vehicleType) {
        switch (vehicleType) {
            case BIKE:  return SpotType.SMALL;
            case CAR:   return SpotType.MEDIUM;
            case TRUCK: return SpotType.LARGE;
            default:    return SpotType.MEDIUM;
        }
    }
    
    /**
     * Find first available spot of given type
     */
    private ParkingSpot findAvailableSpot(SpotType type) {
        List<ParkingSpot> spots = spotsByType.get(type);
        
        for (ParkingSpot spot : spots) {
            if (spot.isAvailable()) {
                return spot;
            }
        }
        return null;
    }
    
    // ==================== QUERIES ====================
    
    public int getAvailableSpots(SpotType type) {
        int count = 0;
        for (ParkingSpot spot : spotsByType.get(type)) {
            if (spot.isAvailable()) count++;
        }
        return count;
    }
    
    public int getTotalSpots() {
        int total = 0;
        for (List<ParkingSpot> spots : spotsByType.values()) {
            total += spots.size();
        }
        return total;
    }
}



package com.parkinglot.strategy;

import com.parkinglot.model.ParkingFloor;
import com.parkinglot.model.ParkingSpot;
import com.parkinglot.enums.VehicleType;
import java.util.List;

/**
 * Strategy Pattern Interface for finding a parking spot.
 * 
 * Why Strategy Pattern?
 * - We might want different algorithms for finding a spot:
 *   1. Nearest to Entry (User convenience)
 *   2. Most Empty Floor (Distribute load)
 *   3. First Available (Performance)
 * - This interface allows us to switch algorithms at runtime without changing the core ParkingLot code.
 */
public interface ParkingAssignmentStrategy {
    ParkingSpot findSpot(List<ParkingFloor> floors, VehicleType vehicleType);
}

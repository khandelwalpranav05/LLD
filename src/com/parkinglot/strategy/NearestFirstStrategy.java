package com.parkinglot.strategy;

import com.parkinglot.model.ParkingFloor;
import com.parkinglot.model.ParkingSpot;
import com.parkinglot.enums.VehicleType;
import com.parkinglot.enums.SpotType;
import java.util.List;

public class NearestFirstStrategy implements ParkingAssignmentStrategy {
    @Override
    public ParkingSpot findSpot(List<ParkingFloor> floors, VehicleType vehicleType) {
        SpotType spotType = getSpotTypeForVehicle(vehicleType);
        
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.getSpot(spotType);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }

    private SpotType getSpotTypeForVehicle(VehicleType vehicleType) {
        switch (vehicleType) {
            case CAR: return SpotType.COMPACT;
            case TRUCK: return SpotType.LARGE;
            case BIKE: return SpotType.MOTORCYCLE;
            case ELECTRIC: return SpotType.ELECTRIC;
            default: return SpotType.COMPACT;
        }
    }
}

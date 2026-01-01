package com.parkinglot.model;

import com.parkinglot.enums.SpotType;

public class MotorcycleSpot extends ParkingSpot {
    public MotorcycleSpot(String id) {
        super(id, SpotType.MOTORCYCLE);
    }
}

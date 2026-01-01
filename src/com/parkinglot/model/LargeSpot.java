package com.parkinglot.model;

import com.parkinglot.enums.SpotType;

public class LargeSpot extends ParkingSpot {
    public LargeSpot(String id) {
        super(id, SpotType.LARGE);
    }
}

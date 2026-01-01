package com.parkinglot.model;

import com.parkinglot.enums.SpotType;

public class CompactSpot extends ParkingSpot {
    public CompactSpot(String id) {
        super(id, SpotType.COMPACT);
    }
}

package com.parkinglot.model;

import com.parkinglot.enums.SpotType;

public class HandicappedSpot extends ParkingSpot {
    public HandicappedSpot(String id) {
        super(id, SpotType.HANDICAPPED);
    }
}

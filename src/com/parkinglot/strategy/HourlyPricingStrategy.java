package com.parkinglot.strategy;

import com.parkinglot.model.Ticket;

public class HourlyPricingStrategy implements PricingStrategy {
    private static final double HOURLY_RATE = 2.0;

    @Override
    public double calculatePrice(Ticket ticket) {
        long durationMillis = System.currentTimeMillis() - ticket.getEntryTime();
        double hours = Math.ceil(durationMillis / (1000.0 * 60 * 60));
        // Minimum 1 hour
        if (hours < 1) hours = 1;
        return hours * HOURLY_RATE;
    }
}

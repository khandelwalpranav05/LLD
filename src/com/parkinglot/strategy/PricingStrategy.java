package com.parkinglot.strategy;

import com.parkinglot.model.Ticket;

/**
 * Strategy Pattern Interface for calculating parking fees.
 * 
 * Why Strategy Pattern?
 * - Pricing rules are complex and change often (e.g., Hourly, Daily, Weekend, Holiday).
 * - We don't want to hardcode pricing logic in the Ticket or ParkingLot class.
 * - This allows us to plug in different pricing models easily.
 */
public interface PricingStrategy {
    double calculatePrice(Ticket ticket);
}

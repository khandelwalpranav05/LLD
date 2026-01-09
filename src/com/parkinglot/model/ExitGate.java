package com.parkinglot.model;

import com.parkinglot.enums.TicketStatus;
import com.parkinglot.service.ParkingLot;
import com.parkinglot.strategy.PricingStrategy;

/**
 * Exit Gate - responsible for:
 * 1. Validating tickets
 * 2. Calculating parking fees
 * 3. Processing payment
 * 4. Releasing the parking spot
 * 5. Recording which gate was used for exit
 * 
 * In a real system, this would also:
 * - Control the physical barrier
 * - Integrate with payment terminals
 * - Handle lost ticket scenarios
 */
public class ExitGate extends Gate {
    
    private PricingStrategy pricingStrategy;
    
    public ExitGate(String id, PricingStrategy pricingStrategy) {
        super(id);
        this.pricingStrategy = pricingStrategy;
    }
    
    /**
     * Process a vehicle exit.
     * 
     * @param ticket The parking ticket
     * @return ExitResult containing fee and status
     */
    public ExitResult processExit(Ticket ticket) {
        // 1. Validate ticket
        if (ticket == null) {
            return new ExitResult(false, 0, "Invalid ticket: null");
        }
        
        if (ticket.getStatus() == TicketStatus.PAID) {
            return new ExitResult(false, 0, "Ticket already paid");
        }
        
        if (ticket.getStatus() == TicketStatus.LOST) {
            // Could apply lost ticket fee here
            return new ExitResult(false, 0, "Lost ticket - please see attendant");
        }
        
        // 2. Calculate fee
        double fee = pricingStrategy.calculatePrice(ticket);
        
        // 3. Mark ticket as paid (in real system, this would be after payment confirmation)
        ticket.markPaid(fee, System.currentTimeMillis(), this.getId());
        
        // 4. Release the parking spot
        // Note: ParkingFloor.returnSpot() handles removeVehicle() internally
        ParkingSpot spot = ticket.getAssignedSpot();
        if (spot != null) {
            ParkingLot.getInstance().returnSpot(spot);
        }
        
        // 5. Return result
        return new ExitResult(true, fee, 
            "Vehicle exited via Gate " + this.getId() + 
            ". Entry was via Gate " + ticket.getEntryGateId() +
            ". Fee: $" + fee);
    }
    
    /**
     * Result object for exit operation.
     */
    public static class ExitResult {
        private boolean success;
        private double fee;
        private String message;
        
        public ExitResult(boolean success, double fee, String message) {
            this.success = success;
            this.fee = fee;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public double getFee() { return fee; }
        public String getMessage() { return message; }
    }
}

package com.elevator.model;

public class Request {
    private int sourceFloor;
    private int destinationFloor;
    private Direction direction;
    private boolean pickedUp;  // NEW: Track if passenger is on board

    public Request(int sourceFloor, int destinationFloor) {
        this.sourceFloor = sourceFloor;
        this.destinationFloor = destinationFloor;
        this.direction = (destinationFloor > sourceFloor) ? Direction.UP : Direction.DOWN;
        this.pickedUp = false;
    }

    public int getSourceFloor() { return sourceFloor; }
    public int getDestinationFloor() { return destinationFloor; }
    public Direction getDirection() { return direction; }
    
    public boolean isPickedUp() { return pickedUp; }
    public void setPickedUp(boolean pickedUp) { this.pickedUp = pickedUp; }
    
    @Override
    public String toString() {
        return sourceFloor + "→" + destinationFloor + (pickedUp ? " [ON BOARD]" : " [WAITING]");
    }
}

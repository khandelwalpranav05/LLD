package com.elevator.simple;

/**
 * Simple request: source floor → destination floor
 * pickedUp tracks if passenger is on board
 */
public class Request {
    public int source;
    public int destination;
    public Direction direction;
    public boolean pickedUp = false;
    
    public Request(int src, int dest) {
        this.source = src;
        this.destination = dest;
        this.direction = dest > src ? Direction.UP : Direction.DOWN;
    }
    
    @Override
    public String toString() {
        return source + "→" + destination + (pickedUp ? " [ON]" : "");
    }
}


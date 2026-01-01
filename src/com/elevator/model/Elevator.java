package com.elevator.model;

import com.elevator.state.ElevatorState;
import com.elevator.state.IdleState;
import java.util.ArrayList;
import java.util.List;

public class Elevator implements Runnable {
    private int id;
    private int currentFloor;
    private ElevatorState state;
    private List<Request> pendingRequests;

    public Elevator(int id) {
        this.id = id;
        this.currentFloor = 0; // Ground Floor
        this.state = new IdleState();
        this.pendingRequests = new ArrayList<>();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                while (pendingRequests.isEmpty()) {
                    try {
                        System.out.println("Elevator " + id + " waiting for requests...");
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            move();
            
            try { Thread.sleep(500); } catch (InterruptedException e) {}
        }
    }

    public synchronized void addRequest(Request request) {
        pendingRequests.add(request);
        System.out.println("Elevator " + id + " received request: " + request.getSourceFloor() + " -> " + request.getDestinationFloor());
        notifyAll(); // Wake up waiting threads if any
    }

    public synchronized void move() {
        state.move(this);
    }

    public synchronized void stop() {
        state.stop(this);
    }

    public synchronized boolean hasRequestAt(int floor) {
        for (Request r : pendingRequests) {
            if (r.getSourceFloor() == floor || r.getDestinationFloor() == floor) {
                return true;
            }
        }
        return false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public synchronized int getCurrentFloor() { return currentFloor; }
    public synchronized void setCurrentFloor(int floor) { this.currentFloor = floor; }
    public synchronized void setState(ElevatorState state) { this.state = state; }
    public synchronized List<Request> getPendingRequests() { return pendingRequests; }
}

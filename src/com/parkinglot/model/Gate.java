package com.parkinglot.model;

public abstract class Gate {
    private String id;
    
    public Gate(String id) {
        this.id = id;
    }
    
    public String getId() { return id; }
}

package com.elevator_v2.model;

import java.util.Objects;

/**
 * Request represents a stop request.
 * - PICKUP_UP/PICKUP_DOWN: Someone pressed hall button
 * - DESTINATION: Someone inside pressed a floor button
 */
public class Request {
    private final int floor;
    private final RequestType type;

    public Request(int floor, RequestType type) {
        this.floor = floor;
        this.type = type;
    }

    public int getFloor() { return floor; }
    public RequestType getType() { return type; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;
        Request request = (Request) o;
        return floor == request.floor && type == request.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(floor, type);
    }

    @Override
    public String toString() {
        return "Request{floor=" + floor + ", type=" + type + "}";
    }
}

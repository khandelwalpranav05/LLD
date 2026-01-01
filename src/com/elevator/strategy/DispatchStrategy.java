package com.elevator.strategy;

import com.elevator.model.Elevator;
import com.elevator.model.Request;
import java.util.List;

public interface DispatchStrategy {
    Elevator selectElevator(List<Elevator> elevators, Request request);
}

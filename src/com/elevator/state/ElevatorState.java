package com.elevator.state;

import com.elevator.model.Elevator;

public interface ElevatorState {
    void move(Elevator elevator);
    void stop(Elevator elevator);
}

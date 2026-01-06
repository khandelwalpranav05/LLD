package com.elevator_v2.model;

/**
 * Request types - From HelloInterview (better than source/dest model)
 * PICKUP_UP: Hall call, user going up
 * PICKUP_DOWN: Hall call, user going down  
 * DESTINATION: Car call, user pressed floor inside elevator
 */
public enum RequestType {
    PICKUP_UP,
    PICKUP_DOWN,
    DESTINATION
}

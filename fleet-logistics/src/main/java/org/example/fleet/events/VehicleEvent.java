package org.example.fleet.events;

import org.example.fleet.observer.Event;

public interface VehicleEvent extends Event {
    String vehicleId();
}

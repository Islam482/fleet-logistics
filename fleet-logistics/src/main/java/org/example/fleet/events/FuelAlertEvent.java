package org.example.fleet.events;

import org.example.fleet.observer.EventType;

import java.util.Objects;

public record FuelAlertEvent(String vehicleId, double litersRemaining) implements VehicleEvent {
    public FuelAlertEvent {
        Objects.requireNonNull(vehicleId);
    }

    @Override
    public EventType type() {
        return EventType.FUEL_ALERT;
    }
}

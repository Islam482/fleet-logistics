package org.example.fleet.events;

import org.example.fleet.observer.EventType;

import java.util.Objects;

public record FuelReadingEvent(String vehicleId, double litersRemaining) implements VehicleEvent {
    public FuelReadingEvent {
        Objects.requireNonNull(vehicleId);
    }

    @Override
    public EventType type() {
        return EventType.FUEL_READING;
    }
}

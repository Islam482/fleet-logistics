package org.example.fleet.events;

import org.example.fleet.model.GeoPoint;
import org.example.fleet.observer.EventType;

import java.util.Objects;

public record GpsPositionEvent(String vehicleId, GeoPoint position, double speedKmh) implements VehicleEvent {
    public GpsPositionEvent {
        Objects.requireNonNull(vehicleId);
        Objects.requireNonNull(position);
    }

    @Override
    public EventType type() {
        return EventType.GPS_POSITION;
    }
}

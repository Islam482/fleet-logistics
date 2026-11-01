package org.example.fleet.events;

import org.example.fleet.model.GeoPoint;
import org.example.fleet.observer.EventType;

import java.util.Objects;

public record GeofenceAlertEvent(String vehicleId, GeoPoint position) implements VehicleEvent {
    public GeofenceAlertEvent {
        Objects.requireNonNull(vehicleId);
        Objects.requireNonNull(position);
    }

    @Override
    public EventType type() {
        return EventType.GEOFENCE_ALERT;
    }
}

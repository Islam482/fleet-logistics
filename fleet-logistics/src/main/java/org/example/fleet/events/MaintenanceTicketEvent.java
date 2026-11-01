package org.example.fleet.events;

import org.example.fleet.observer.EventType;

import java.util.List;
import java.util.Objects;

public record MaintenanceTicketEvent(String vehicleId, String depotId, EventType trigger,
                                     List<String> overdueAssetIds, double estimatedMonthlyCost)
        implements VehicleEvent {
    public MaintenanceTicketEvent {
        Objects.requireNonNull(vehicleId);
        Objects.requireNonNull(depotId);
        Objects.requireNonNull(trigger);
        overdueAssetIds = List.copyOf(overdueAssetIds);
    }

    @Override
    public EventType type() {
        return EventType.MAINTENANCE_TICKET;
    }
}

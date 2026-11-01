package org.example.fleet.events;

import org.example.fleet.observer.Event;
import org.example.fleet.observer.EventType;

import java.util.Objects;

public record DepotSummaryEvent(String depotId, long eventCount, double averageSpeedKmh, long fuelReadings)
        implements Event {
    public DepotSummaryEvent {
        Objects.requireNonNull(depotId);
    }

    @Override
    public EventType type() {
        return EventType.DEPOT_SUMMARY;
    }
}

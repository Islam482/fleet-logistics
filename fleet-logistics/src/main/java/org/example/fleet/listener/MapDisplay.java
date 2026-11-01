package org.example.fleet.listener;

import org.example.fleet.events.GpsPositionEvent;
import org.example.fleet.observer.Observer;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class MapDisplay implements Observer<GpsPositionEvent> {
    private final Map<String, GpsPositionEvent> lastPositions = new ConcurrentHashMap<>();
    private final LongAdder updates = new LongAdder();

    @Override
    public void onEvent(GpsPositionEvent event) {
        lastPositions.put(event.vehicleId(), event);
        updates.increment();
    }

    public Optional<GpsPositionEvent> lastPosition(String vehicleId) {
        return Optional.ofNullable(lastPositions.get(vehicleId));
    }

    public long updates() {
        return updates.sum();
    }
}

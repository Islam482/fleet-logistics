package org.example.fleet.source;

import org.example.fleet.events.GpsPositionEvent;
import org.example.fleet.model.GeoPoint;
import org.example.fleet.observer.EventHub;

import java.util.function.Consumer;

public final class GpsFeed extends EventHub<GpsPositionEvent> {
    public GpsFeed(Consumer<RuntimeException> failureHandler) {
        super(failureHandler);
    }

    public void report(String vehicleId, GeoPoint position, double speedKmh) {
        publish(new GpsPositionEvent(vehicleId, position, speedKmh));
    }
}

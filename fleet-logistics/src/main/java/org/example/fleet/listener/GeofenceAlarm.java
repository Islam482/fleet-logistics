package org.example.fleet.listener;

import org.example.fleet.events.GeofenceAlertEvent;
import org.example.fleet.events.GpsPositionEvent;
import org.example.fleet.events.VehicleEvent;
import org.example.fleet.model.GeoZone;
import org.example.fleet.observer.EventHub;
import org.example.fleet.observer.Observer;

import java.util.concurrent.atomic.LongAdder;

public final class GeofenceAlarm implements Observer<GpsPositionEvent> {
    private final GeoZone zone;
    private final EventHub<VehicleEvent> alerts;
    private final LongAdder breaches = new LongAdder();

    public GeofenceAlarm(GeoZone zone, EventHub<VehicleEvent> alerts) {
        this.zone = zone;
        this.alerts = alerts;
    }

    @Override
    public void onEvent(GpsPositionEvent event) {
        if (zone.contains(event.position())) {
            return;
        }
        breaches.increment();
        alerts.publish(new GeofenceAlertEvent(event.vehicleId(), event.position()));
    }

    public long breaches() {
        return breaches.sum();
    }
}

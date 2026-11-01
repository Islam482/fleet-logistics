package org.example.fleet.listener;

import org.example.fleet.events.GpsPositionEvent;
import org.example.fleet.model.GeoPoint;
import org.example.fleet.observer.Observer;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;

public final class EtaCalculator implements Observer<GpsPositionEvent> {
    private final Map<String, GeoPoint> destinations;
    private final Map<String, Double> etaMinutes = new ConcurrentHashMap<>();

    public EtaCalculator(Map<String, GeoPoint> destinations) {
        this.destinations = Map.copyOf(destinations);
    }

    @Override
    public void onEvent(GpsPositionEvent event) {
        GeoPoint destination = destinations.get(event.vehicleId());
        if (destination == null) {
            return;
        }
        double distanceKm = event.position().distanceKm(destination);
        double minutes = event.speedKmh() > 0 ? distanceKm / event.speedKmh() * 60.0 : Double.POSITIVE_INFINITY;
        etaMinutes.put(event.vehicleId(), minutes);
    }

    public OptionalDouble etaMinutes(String vehicleId) {
        Double eta = etaMinutes.get(vehicleId);
        return eta == null ? OptionalDouble.empty() : OptionalDouble.of(eta);
    }
}

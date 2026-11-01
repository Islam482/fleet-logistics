package org.example.fleet;

import org.example.fleet.model.Fleet;
import org.example.fleet.model.GeoPoint;
import org.example.fleet.model.GeoZone;

import java.util.Map;

public record FleetScenario(Fleet fleet, GeoZone zone, Map<String, GeoPoint> destinations) {
    public FleetScenario {
        destinations = Map.copyOf(destinations);
    }
}

package org.example.fleet;

import org.example.fleet.model.ChargingStation;
import org.example.fleet.model.Depot;
import org.example.fleet.model.Drone;
import org.example.fleet.model.Fleet;
import org.example.fleet.model.GeoPoint;
import org.example.fleet.model.GeoZone;
import org.example.fleet.model.Truck;
import org.example.fleet.model.Van;

import java.util.List;
import java.util.Map;

public final class SampleData {
    private SampleData() {
    }

    public static FleetScenario scenario() {
        Depot north = new Depot("D-NORTH", "North Depot", List.of(
                new Truck("T1", 4000, 32.0, 0.15, 45),
                new Truck("T2", 3500, 30.0, 0.14, 100),
                new Van("V1", 2500, 9.0, 0.08, 60),
                new Drone("DR1", 80, 0.8, 1.20, 70),
                new ChargingStation("C1", 120, 200)));
        Depot south = new Depot("D-SOUTH", "South Depot", List.of(
                new Truck("T3", 5000, 35.0, 0.16, 30),
                new Van("V2", 3000, 8.5, 0.07, 130),
                new Van("V3", 2000, 9.5, 0.09, 10),
                new Drone("DR2", 60, 0.8, 1.20, 20),
                new ChargingStation("C2", 120, 10)));
        Fleet fleet = new Fleet("F1", "Almaty Logistics", List.of(north, south));

        GeoPoint almaty = new GeoPoint(43.238, 76.945);
        GeoZone zone = new GeoZone(almaty, 15.0);
        Map<String, GeoPoint> destinations = Map.of(
                "T1", new GeoPoint(43.30, 77.00),
                "T2", new GeoPoint(43.20, 76.90),
                "V1", new GeoPoint(43.25, 76.95),
                "DR1", new GeoPoint(43.24, 76.96),
                "T3", new GeoPoint(43.15, 76.85),
                "V2", new GeoPoint(43.22, 76.99),
                "V3", new GeoPoint(43.26, 76.93),
                "DR2", new GeoPoint(43.23, 76.94));
        return new FleetScenario(fleet, zone, destinations);
    }
}

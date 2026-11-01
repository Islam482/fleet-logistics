package org.example.fleet.model;

public record GeoZone(GeoPoint center, double radiusKm) {
    public boolean contains(GeoPoint point) {
        return center.distanceKm(point) <= radiusKm;
    }
}

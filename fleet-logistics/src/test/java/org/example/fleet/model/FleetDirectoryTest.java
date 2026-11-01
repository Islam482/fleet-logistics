package org.example.fleet.model;

import org.example.fleet.SampleData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetDirectoryTest {
    @Test
    void findsTheDepotOfEveryAsset() {
        FleetDirectory directory = new FleetDirectory(SampleData.scenario().fleet());

        assertEquals("D-NORTH", directory.depotOf("T1").orElseThrow().id());
        assertEquals("D-SOUTH", directory.depotOf("V3").orElseThrow().id());
        assertEquals(2, directory.depots().size());
    }

    @Test
    void unknownAssetHasNoDepot() {
        FleetDirectory directory = new FleetDirectory(SampleData.scenario().fleet());
        assertTrue(directory.depotOf("nope").isEmpty());
    }

    @Test
    void duplicateAssetIdsAreRejected() {
        Fleet fleet = new Fleet("F", "dup", List.of(
                new Depot("A", "a", List.of(new ChargingStation("X", 1, 1))),
                new Depot("B", "b", List.of(new ChargingStation("X", 1, 1)))));

        assertThrows(IllegalArgumentException.class, () -> new FleetDirectory(fleet));
    }

    @Test
    void geoPointDistanceMatchesOneDegreeOfLongitudeAtTheEquator() {
        assertEquals(111.1949, new GeoPoint(0, 0).distanceKm(new GeoPoint(0, 1)), 0.001);
    }

    @Test
    void zoneIncludesItsBorderAndExcludesFartherPoints() {
        GeoZone zone = new GeoZone(new GeoPoint(0, 0), 10.0);
        assertTrue(zone.contains(new GeoPoint(0, 0.05)));
        assertFalse(zone.contains(new GeoPoint(0, 0.2)));
    }
}

package org.example.fleet;

import org.example.fleet.observer.EventType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompositionRootTest {
    @Test
    void wiresEveryListenerToTheSourceAndTypeItNeeds() {
        try (FleetSystem system = CompositionRoot.assemble(
                new FleetConfig(1, 0, 1), SampleData.scenario(), e -> { })) {
            assertEquals(4, system.gpsFeed().observerCount(EventType.GPS_POSITION));
            assertEquals(2, system.fuelFeed().observerCount(EventType.FUEL_READING));
            assertEquals(1, system.alerts().observerCount(EventType.GEOFENCE_ALERT));
            assertEquals(1, system.alerts().observerCount(EventType.FUEL_ALERT));
            assertEquals(1, system.depotAggregator().observerCount(EventType.DEPOT_SUMMARY));
            assertEquals(1, system.alertService().observerCount(EventType.MAINTENANCE_TICKET));
            assertEquals(0, system.gpsFeed().observerCount(EventType.FUEL_READING));
        }
    }

    @Test
    void configRejectsFewerThanOneProducerThread() {
        assertThrows(IllegalArgumentException.class, () -> new FleetConfig(0, 10, 1));
    }
}

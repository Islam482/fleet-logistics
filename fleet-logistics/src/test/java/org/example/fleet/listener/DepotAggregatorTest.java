package org.example.fleet.listener;

import org.example.fleet.SampleData;
import org.example.fleet.events.DepotSummaryEvent;
import org.example.fleet.model.FleetDirectory;
import org.example.fleet.model.GeoPoint;
import org.example.fleet.observer.EventType;
import org.example.fleet.source.FuelFeed;
import org.example.fleet.source.GpsFeed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepotAggregatorTest {
    private final GpsFeed gps = new GpsFeed(e -> { });
    private final FuelFeed fuel = new FuelFeed(e -> { });
    private final FleetDirectory directory = new FleetDirectory(SampleData.scenario().fleet());
    private final DepotAggregator aggregator = new DepotAggregator(directory, 3, e -> { });
    private final List<DepotSummaryEvent> summaries = new CopyOnWriteArrayList<>();

    DepotAggregatorTest() {
        gps.subscribe(EventType.GPS_POSITION, aggregator);
        fuel.subscribe(EventType.FUEL_READING, aggregator);
        aggregator.subscribe(EventType.DEPOT_SUMMARY, summaries::add);
    }

    @Test
    void republishesOneSummaryEveryThirdEventOfADepot() {
        gps.report("T1", new GeoPoint(0, 0), 10);
        gps.report("V1", new GeoPoint(0, 0), 30);
        assertTrue(summaries.isEmpty());

        fuel.report("T2", 80);

        assertEquals(1, summaries.size());
        DepotSummaryEvent summary = summaries.get(0);
        assertEquals("D-NORTH", summary.depotId());
        assertEquals(3, summary.eventCount());
        assertEquals(20.0, summary.averageSpeedKmh(), 1e-9);
        assertEquals(1, summary.fuelReadings());
    }

    @Test
    void depotsAreCountedSeparately() {
        gps.report("T1", new GeoPoint(0, 0), 10);
        gps.report("T3", new GeoPoint(0, 0), 10);
        gps.report("V2", new GeoPoint(0, 0), 10);

        assertEquals(1, aggregator.eventsOf("D-NORTH"));
        assertEquals(2, aggregator.eventsOf("D-SOUTH"));
        assertTrue(summaries.isEmpty());
    }

    @Test
    void chainReachesTheDashboardThroughTheAggregatorAsSubject() {
        DepotDashboard dashboard = new DepotDashboard();
        aggregator.subscribe(EventType.DEPOT_SUMMARY, dashboard);

        for (int i = 0; i < 6; i++) {
            gps.report("T3", new GeoPoint(0, 0), i);
        }

        assertEquals(2, dashboard.received());
        assertEquals(6, dashboard.newestOf("D-SOUTH").orElseThrow().eventCount());
        assertTrue(dashboard.newestOf("D-NORTH").isEmpty());
    }

    @Test
    void eventsOfUnknownVehiclesAreCountedAsIgnoredNotLost() {
        gps.report("GHOST", new GeoPoint(0, 0), 10);

        assertEquals(1, aggregator.ignoredEvents());
        assertEquals(0, aggregator.eventsOf("D-NORTH") + aggregator.eventsOf("D-SOUTH"));
    }

    @Test
    void dashboardKeepsTheNewerSummaryWhenTheyArriveOutOfOrder() {
        DepotDashboard dashboard = new DepotDashboard();

        dashboard.onEvent(new DepotSummaryEvent("D", 200, 1.0, 0));
        dashboard.onEvent(new DepotSummaryEvent("D", 100, 2.0, 0));

        assertEquals(200, dashboard.newestOf("D").orElseThrow().eventCount());
        assertEquals(2, dashboard.received());
    }

    @Test
    void summaryIntervalMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new DepotAggregator(directory, 0, e -> { }));
    }
}

package org.example.fleet.listener;

import org.example.fleet.events.FuelAlertEvent;
import org.example.fleet.events.GeofenceAlertEvent;
import org.example.fleet.events.GpsPositionEvent;
import org.example.fleet.events.VehicleEvent;
import org.example.fleet.model.GeoPoint;
import org.example.fleet.model.GeoZone;
import org.example.fleet.observer.EventHub;
import org.example.fleet.observer.EventType;
import org.example.fleet.source.FuelFeed;
import org.example.fleet.source.GpsFeed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedListenersTest {
    private final GpsFeed gps = new GpsFeed(e -> { });
    private final FuelFeed fuel = new FuelFeed(e -> { });
    private final EventHub<VehicleEvent> alerts = new EventHub<>(e -> { });
    private final List<VehicleEvent> alertLog = new CopyOnWriteArrayList<>();

    FeedListenersTest() {
        alerts.subscribe(EventType.GEOFENCE_ALERT, alertLog::add);
        alerts.subscribe(EventType.FUEL_ALERT, alertLog::add);
    }

    @Test
    void mapDisplayKeepsTheLatestPositionPerVehicle() {
        MapDisplay map = new MapDisplay();
        gps.subscribe(EventType.GPS_POSITION, map);

        gps.report("T1", new GeoPoint(1, 1), 10);
        gps.report("T1", new GeoPoint(2, 2), 20);
        gps.report("T2", new GeoPoint(3, 3), 30);

        assertEquals(new GeoPoint(2, 2), map.lastPosition("T1").orElseThrow().position());
        assertEquals(new GeoPoint(3, 3), map.lastPosition("T2").orElseThrow().position());
        assertEquals(3, map.updates());
        assertTrue(map.lastPosition("T9").isEmpty());
    }

    @Test
    void etaIsDistanceOverSpeed() {
        EtaCalculator eta = new EtaCalculator(Map.of("T1", new GeoPoint(0, 1)));
        gps.subscribe(EventType.GPS_POSITION, eta);

        gps.report("T1", new GeoPoint(0, 0), 60);

        assertEquals(111.1949, eta.etaMinutes("T1").orElseThrow(), 0.001);
    }

    @Test
    void stoppedVehicleHasInfiniteEtaAndUntrackedVehicleHasNone() {
        EtaCalculator eta = new EtaCalculator(Map.of("T1", new GeoPoint(0, 1)));
        gps.subscribe(EventType.GPS_POSITION, eta);

        gps.report("T1", new GeoPoint(0, 0), 0);
        gps.report("T2", new GeoPoint(0, 0), 50);

        assertEquals(Double.POSITIVE_INFINITY, eta.etaMinutes("T1").orElseThrow());
        assertTrue(eta.etaMinutes("T2").isEmpty());
    }

    @Test
    void geofenceAlarmRaisesAlertsOnlyOutsideTheZone() {
        GeofenceAlarm alarm = new GeofenceAlarm(new GeoZone(new GeoPoint(0, 0), 10.0), alerts);
        gps.subscribe(EventType.GPS_POSITION, alarm);

        gps.report("T1", new GeoPoint(0, 0.05), 10);
        gps.report("T2", new GeoPoint(0, 0.2), 10);

        assertEquals(1, alarm.breaches());
        assertEquals(1, alertLog.size());
        GeofenceAlertEvent alert = (GeofenceAlertEvent) alertLog.get(0);
        assertEquals("T2", alert.vehicleId());
        assertEquals(new GeoPoint(0, 0.2), alert.position());
    }

    @Test
    void fuelMonitorAlertsStrictlyBelowTheThreshold() {
        FuelMonitor monitor = new FuelMonitor(20.0, alerts);
        fuel.subscribe(EventType.FUEL_READING, monitor);

        fuel.report("T1", 20.0);
        fuel.report("T1", 19.9);
        fuel.report("T2", 100.0);

        assertEquals(3, monitor.readings());
        assertEquals(1, monitor.lowFuelAlerts());
        assertEquals(new FuelAlertEvent("T1", 19.9), alertLog.get(0));
    }

    @Test
    void oneSourceFeedsManyListenersWithTheSameEventObject() {
        List<GpsPositionEvent> first = new CopyOnWriteArrayList<>();
        List<GpsPositionEvent> second = new CopyOnWriteArrayList<>();
        gps.subscribe(EventType.GPS_POSITION, first::add);
        gps.subscribe(EventType.GPS_POSITION, second::add);

        gps.report("T1", new GeoPoint(5, 5), 1);

        assertEquals(1, first.size());
        assertTrue(first.get(0) == second.get(0));
    }
}

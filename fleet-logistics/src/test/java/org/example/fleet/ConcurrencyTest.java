package org.example.fleet;

import org.example.fleet.model.GeoPoint;
import org.example.fleet.observer.EventType;
import org.example.fleet.source.GpsFeed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrencyTest {
    private static final int THREADS = 4;
    private static final int EVENTS_EACH = 1000;

    private final List<RuntimeException> failures = new CopyOnWriteArrayList<>();

    @Test
    @Timeout(60)
    void fourThreadsTimesAThousandEventsReachEveryListenerExactlyOnce() throws Exception {
        GpsFeed feed = new GpsFeed(failures::add);
        LongAdder[] listenerCounts = new LongAdder[5];
        for (int i = 0; i < listenerCounts.length; i++) {
            LongAdder counter = new LongAdder();
            listenerCounts[i] = counter;
            feed.subscribe(EventType.GPS_POSITION, event -> counter.increment());
        }

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startGate = new CountDownLatch(1);
        try {
            List<Future<?>> producers = new ArrayList<>();
            for (int t = 0; t < THREADS; t++) {
                String vehicle = "T" + t;
                producers.add(pool.submit(() -> {
                    startGate.await();
                    for (int i = 0; i < EVENTS_EACH; i++) {
                        feed.report(vehicle, new GeoPoint(0, 0), i);
                    }
                    return null;
                }));
            }
            startGate.countDown();
            for (Future<?> producer : producers) {
                producer.get();
            }
        } finally {
            pool.shutdownNow();
        }

        for (LongAdder counter : listenerCounts) {
            assertEquals(THREADS * EVENTS_EACH, counter.sum());
        }
        assertTrue(failures.isEmpty());
    }

    @Test
    @Timeout(60)
    void wholePipelineCountsAddUpExactlyUnderLoad() {
        FleetSystem system = CompositionRoot.assemble(
                new FleetConfig(THREADS, EVENTS_EACH, 42), SampleData.scenario(), failures::add);
        LongAdder geofenceAlerts = new LongAdder();
        LongAdder fuelAlerts = new LongAdder();
        system.alerts().subscribe(EventType.GEOFENCE_ALERT, e -> geofenceAlerts.increment());
        system.alerts().subscribe(EventType.FUEL_ALERT, e -> fuelAlerts.increment());

        try (system) {
            system.simulator().run();
        }

        long gpsEvents = (long) THREADS * EVENTS_EACH / 2;
        long fuelReadings = (long) THREADS * EVENTS_EACH / 2;
        assertEquals(gpsEvents, system.mapDisplay().updates());
        assertEquals(fuelReadings, system.fuelMonitor().readings());
        assertEquals(geofenceAlerts.sum(), system.geofenceAlarm().breaches());
        assertEquals(fuelAlerts.sum(), system.fuelMonitor().lowFuelAlerts());

        long alerts = geofenceAlerts.sum() + fuelAlerts.sum();
        assertTrue(alerts > 0);

        assertEquals(alerts, system.alertService().inspectionsRun());
        assertEquals(alerts, system.ticketLog().tickets().size());

        long north = system.depotAggregator().eventsOf("D-NORTH");
        long south = system.depotAggregator().eventsOf("D-SOUTH");
        assertEquals(gpsEvents + fuelReadings, north + south);
        assertEquals(north / 100 + south / 100, system.depotDashboard().received());
        assertTrue(failures.isEmpty());
    }

    @Test
    @Timeout(60)
    void producerThreadCountIsConfigurable() {
        for (int threads : new int[] {1, 2, 4}) {
            FleetSystem system = CompositionRoot.assemble(
                    new FleetConfig(threads, 200, 7), SampleData.scenario(), failures::add);
            try (system) {
                system.simulator().run();
            }
            assertEquals(threads * 100L, system.mapDisplay().updates(), "threads=" + threads);
            assertEquals(threads * 100L, system.fuelMonitor().readings(), "threads=" + threads);
        }
    }

    @Test
    @Timeout(60)
    void sameSeedGivesTheSameResultRegardlessOfThreadScheduling() {
        long firstBreaches = breachesOfOneRun();
        long secondBreaches = breachesOfOneRun();

        assertEquals(firstBreaches, secondBreaches);
        assertTrue(firstBreaches > 0);
    }

    @Test
    @Timeout(60)
    void gracefulShutdownDeliversEverythingAndLeavesNoThreadBehind() throws Exception {
        FleetSystem system = CompositionRoot.assemble(
                new FleetConfig(THREADS, EVENTS_EACH, 1), SampleData.scenario(), failures::add);

        List<Future<?>> producers = system.simulator().start();
        system.close();

        for (Future<?> producer : producers) {
            assertTrue(producer.isDone());
        }
        assertEquals((long) THREADS * EVENTS_EACH / 2, system.mapDisplay().updates());
        assertTrue(waitUntilNoProducerThreadIsAlive(), "producer threads are still running");
    }

    @Test
    @Timeout(60)
    void aFailingListenerNeverStopsProducersOrOtherListeners() {
        GpsFeed feed = new GpsFeed(failures::add);
        LongAdder healthy = new LongAdder();
        feed.subscribe(EventType.GPS_POSITION, event -> {
            throw new IllegalStateException("listener bug");
        });
        feed.subscribe(EventType.GPS_POSITION, event -> healthy.increment());

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            List<Future<?>> producers = new ArrayList<>();
            for (int t = 0; t < THREADS; t++) {
                producers.add(pool.submit(() -> {
                    for (int i = 0; i < EVENTS_EACH; i++) {
                        feed.report("T1", new GeoPoint(0, 0), 1);
                    }
                }));
            }
            for (Future<?> producer : producers) {
                producer.get();
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            pool.shutdownNow();
        }

        assertEquals(THREADS * EVENTS_EACH, healthy.sum());
        assertEquals(THREADS * EVENTS_EACH, failures.size());
    }

    private long breachesOfOneRun() {
        FleetSystem system = CompositionRoot.assemble(
                new FleetConfig(THREADS, 500, 99), SampleData.scenario(), failures::add);
        try (system) {
            system.simulator().run();
        }
        return system.geofenceAlarm().breaches();
    }

    private static boolean waitUntilNoProducerThreadIsAlive() throws InterruptedException {
        for (int attempt = 0; attempt < 100; attempt++) {
            boolean anyAlive = Thread.getAllStackTraces().keySet().stream()
                    .anyMatch(thread -> thread.getName().startsWith("producer-") && thread.isAlive());
            if (!anyAlive) {
                return true;
            }
            Thread.sleep(20);
        }
        return false;
    }
}

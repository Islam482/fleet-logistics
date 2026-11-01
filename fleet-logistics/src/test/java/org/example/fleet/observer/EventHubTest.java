package org.example.fleet.observer;

import org.example.fleet.events.FuelReadingEvent;
import org.example.fleet.events.GpsPositionEvent;
import org.example.fleet.events.MaintenanceTicketEvent;
import org.example.fleet.events.VehicleEvent;
import org.example.fleet.model.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventHubTest {
    private final List<RuntimeException> failures = new CopyOnWriteArrayList<>();
    private final EventHub<VehicleEvent> hub = new EventHub<>(failures::add);

    private static FuelReadingEvent fuel() {
        return new FuelReadingEvent("T1", 50.0);
    }

    private static GpsPositionEvent gps() {
        return new GpsPositionEvent("T1", new GeoPoint(0, 0), 10.0);
    }

    private static Observer<VehicleEvent> counting(LongAdder counter) {
        return event -> counter.increment();
    }

    @Test
    void deliversOnlyToObserversOfTheEventType() {
        LongAdder fuelListener = new LongAdder();
        LongAdder gpsListener = new LongAdder();
        hub.subscribe(EventType.FUEL_READING, counting(fuelListener));
        hub.subscribe(EventType.GPS_POSITION, counting(gpsListener));

        hub.publish(fuel());

        assertEquals(1, fuelListener.sum());
        assertEquals(0, gpsListener.sum());
    }

    @Test
    void sameObserverSubscribedTwiceReceivesEachEventOnce() {
        LongAdder counter = new LongAdder();
        Observer<VehicleEvent> observer = counting(counter);
        hub.subscribe(EventType.FUEL_READING, observer);
        hub.subscribe(EventType.FUEL_READING, observer);

        hub.publish(fuel());

        assertEquals(1, counter.sum());
        assertEquals(1, hub.observerCount(EventType.FUEL_READING));
    }

    @Test
    void unsubscribedObserverReceivesNothingMore() {
        LongAdder counter = new LongAdder();
        Observer<VehicleEvent> observer = counting(counter);
        hub.subscribe(EventType.FUEL_READING, observer);
        hub.publish(fuel());

        hub.unsubscribe(EventType.FUEL_READING, observer);
        hub.publish(fuel());

        assertEquals(1, counter.sum());
    }

    @Test
    void observerMayUnsubscribeItselfInsideItsHandlerWithoutDisturbingOthers() {
        LongAdder selfRemovingCalls = new LongAdder();
        LongAdder neighbourCalls = new LongAdder();
        hub.subscribe(EventType.FUEL_READING, new Observer<VehicleEvent>() {
            @Override
            public void onEvent(VehicleEvent event) {
                selfRemovingCalls.increment();
                hub.unsubscribe(EventType.FUEL_READING, this);
            }
        });
        hub.subscribe(EventType.FUEL_READING, counting(neighbourCalls));

        hub.publish(fuel());
        hub.publish(fuel());

        assertEquals(1, selfRemovingCalls.sum());
        assertEquals(2, neighbourCalls.sum());
        assertEquals(1, hub.observerCount(EventType.FUEL_READING));
        assertEquals(0, failures.size());
    }

    @Test
    void observerSubscribedInsideAHandlerSeesOnlyLaterEvents() {
        LongAdder lateCalls = new LongAdder();
        Observer<VehicleEvent> late = counting(lateCalls);
        hub.subscribe(EventType.FUEL_READING, event -> hub.subscribe(EventType.FUEL_READING, late));

        hub.publish(fuel());
        assertEquals(0, lateCalls.sum());

        hub.publish(fuel());
        assertEquals(1, lateCalls.sum());
    }

    @Test
    void failingObserverIsReportedAndDoesNotBlockTheNext() {
        LongAdder healthyCalls = new LongAdder();
        hub.subscribe(EventType.FUEL_READING, event -> {
            throw new IllegalStateException("boom");
        });
        hub.subscribe(EventType.FUEL_READING, counting(healthyCalls));

        hub.publish(fuel());

        assertEquals(1, healthyCalls.sum());
        assertEquals(1, failures.size());
        assertEquals("boom", failures.get(0).getMessage());
    }

    @Test
    void eventsAreImmutableCopies() {
        List<String> ids = new ArrayList<>(List.of("T2"));
        MaintenanceTicketEvent ticket =
                new MaintenanceTicketEvent("T1", "D-NORTH", EventType.FUEL_ALERT, ids, 10.0);

        ids.add("T9");

        assertEquals(List.of("T2"), ticket.overdueAssetIds());
        assertThrows(UnsupportedOperationException.class, () -> ticket.overdueAssetIds().add("T9"));
    }

    @Test
    @Timeout(60)
    void unsubscribingWhileSeveralThreadsPublishLosesNoEventForStableObservers() throws Exception {
        int publishers = 4;
        int eventsEach = 1000;
        LongAdder stable = new LongAdder();
        hub.subscribe(EventType.FUEL_READING, counting(stable));

        CountDownLatch startGate = new CountDownLatch(1);
        AtomicBoolean publishing = new AtomicBoolean(true);
        ExecutorService pool = Executors.newFixedThreadPool(publishers + 1);
        try {
            Future<?> churn = pool.submit(() -> {
                LongAdder transientCalls = new LongAdder();
                Observer<VehicleEvent> transientObserver = counting(transientCalls);
                awaitQuietly(startGate);
                while (publishing.get()) {
                    hub.subscribe(EventType.FUEL_READING, transientObserver);
                    hub.unsubscribe(EventType.FUEL_READING, transientObserver);
                }
            });
            List<Future<?>> producers = new ArrayList<>();
            for (int p = 0; p < publishers; p++) {
                producers.add(pool.submit(() -> {
                    awaitQuietly(startGate);
                    for (int i = 0; i < eventsEach; i++) {
                        hub.publish(fuel());
                    }
                }));
            }
            startGate.countDown();
            for (Future<?> producer : producers) {
                producer.get();
            }
            publishing.set(false);
            churn.get();
        } finally {
            pool.shutdownNow();
        }

        assertEquals(publishers * eventsEach, stable.sum());
        assertEquals(0, failures.size());
        assertEquals(1, hub.observerCount(EventType.FUEL_READING));
    }

    @Test
    void gpsEventsDoNotReachFuelObservers() {
        LongAdder counter = new LongAdder();
        hub.subscribe(EventType.FUEL_READING, counting(counter));
        hub.publish(gps());
        assertEquals(0, counter.sum());
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

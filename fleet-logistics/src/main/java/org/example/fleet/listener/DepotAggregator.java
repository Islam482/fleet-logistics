package org.example.fleet.listener;

import org.example.fleet.events.DepotSummaryEvent;
import org.example.fleet.events.FuelReadingEvent;
import org.example.fleet.events.GpsPositionEvent;
import org.example.fleet.events.VehicleEvent;
import org.example.fleet.model.Depot;
import org.example.fleet.model.FleetDirectory;
import org.example.fleet.observer.EventHub;
import org.example.fleet.observer.Observer;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

public final class DepotAggregator extends EventHub<DepotSummaryEvent> implements Observer<VehicleEvent> {
    private record DepotStats(long gpsCount, double speedSumKmh, long fuelCount) {
        static final DepotStats EMPTY = new DepotStats(0, 0.0, 0);

        long total() {
            return gpsCount + fuelCount;
        }

        DepotStats withGps(double speedKmh) {
            return new DepotStats(gpsCount + 1, speedSumKmh + speedKmh, fuelCount);
        }

        DepotStats withFuel() {
            return new DepotStats(gpsCount, speedSumKmh, fuelCount + 1);
        }
    }

    private final FleetDirectory directory;
    private final int summaryEvery;
    private final Map<String, DepotStats> statsByDepot = new ConcurrentHashMap<>();
    private final LongAdder ignored = new LongAdder();

    public DepotAggregator(FleetDirectory directory, int summaryEvery, Consumer<RuntimeException> failureHandler) {
        super(failureHandler);
        if (summaryEvery < 1) {
            throw new IllegalArgumentException("summaryEvery must be >= 1");
        }
        this.directory = directory;
        this.summaryEvery = summaryEvery;
        for (Depot depot : directory.depots()) {
            statsByDepot.put(depot.id(), DepotStats.EMPTY);
        }
    }

    @Override
    public void onEvent(VehicleEvent event) {
        Optional<Depot> depot = directory.depotOf(event.vehicleId());
        if (depot.isEmpty()) {
            ignored.increment();
            return;
        }
        String depotId = depot.get().id();
        DepotStats updated;
        if (event instanceof GpsPositionEvent gps) {
            updated = statsByDepot.compute(depotId, (id, stats) -> stats.withGps(gps.speedKmh()));
        } else if (event instanceof FuelReadingEvent) {
            updated = statsByDepot.compute(depotId, (id, stats) -> stats.withFuel());
        } else {
            return;
        }
        if (updated.total() % summaryEvery == 0) {
            double averageSpeed = updated.gpsCount() == 0 ? 0.0 : updated.speedSumKmh() / updated.gpsCount();
            publish(new DepotSummaryEvent(depotId, updated.total(), averageSpeed, updated.fuelCount()));
        }
    }

    public long eventsOf(String depotId) {
        return statsByDepot.get(depotId).total();
    }

    public long ignoredEvents() {
        return ignored.sum();
    }
}

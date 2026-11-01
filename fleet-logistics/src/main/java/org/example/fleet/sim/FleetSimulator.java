package org.example.fleet.sim;

import org.example.fleet.FleetConfig;
import org.example.fleet.model.GeoPoint;
import org.example.fleet.model.GeoZone;
import org.example.fleet.source.FuelFeed;
import org.example.fleet.source.GpsFeed;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class FleetSimulator implements AutoCloseable {
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 30;
    private static final double MAX_SPEED_KMH = 90.0;
    private static final double MAX_FUEL_LITERS = 200.0;
    private static final double KM_PER_DEGREE = 111.0;

    private final ExecutorService pool;
    private final GpsFeed gpsFeed;
    private final FuelFeed fuelFeed;
    private final List<String> vehicleIds;
    private final GeoZone area;
    private final FleetConfig config;

    public FleetSimulator(ExecutorService pool, GpsFeed gpsFeed, FuelFeed fuelFeed,
                          List<String> vehicleIds, GeoZone area, FleetConfig config) {
        if (vehicleIds.isEmpty()) {
            throw new IllegalArgumentException("No vehicles to simulate");
        }
        this.pool = pool;
        this.gpsFeed = gpsFeed;
        this.fuelFeed = fuelFeed;
        this.vehicleIds = List.copyOf(vehicleIds);
        this.area = area;
        this.config = config;
    }

    public List<Future<?>> start() {
        List<Future<?>> producers = new ArrayList<>();
        for (int producer = 0; producer < config.producerThreads(); producer++) {
            int index = producer;
            producers.add(pool.submit(() -> produce(index)));
        }
        return producers;
    }

    public void run() {
        try {
            for (Future<?> producer : start()) {
                producer.get();
            }
        } catch (ExecutionException e) {
            throw new IllegalStateException("A producer failed", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for producers", e);
        }
    }

    @Override
    public void close() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Producer threads did not stop");
                }
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void produce(int producerIndex) {
        SplittableRandom random = new SplittableRandom(config.seed() + producerIndex);
        double spreadDegrees = area.radiusKm() * 1.5 / KM_PER_DEGREE;
        for (int step = 0; step < config.eventsPerProducer(); step++) {
            String vehicleId = vehicleIds.get((producerIndex + step) % vehicleIds.size());
            if (step % 2 == 0) {
                GeoPoint position = new GeoPoint(
                        area.center().latitude() + (random.nextDouble() * 2 - 1) * spreadDegrees,
                        area.center().longitude() + (random.nextDouble() * 2 - 1) * spreadDegrees);
                gpsFeed.report(vehicleId, position, random.nextDouble() * MAX_SPEED_KMH);
            } else {
                fuelFeed.report(vehicleId, random.nextDouble() * MAX_FUEL_LITERS);
            }
        }
    }
}

package org.example.fleet;

public record FleetConfig(int producerThreads, int eventsPerProducer, long seed) {
    public FleetConfig {
        if (producerThreads < 1) {
            throw new IllegalArgumentException("producerThreads must be >= 1");
        }
        if (eventsPerProducer < 0) {
            throw new IllegalArgumentException("eventsPerProducer must be >= 0");
        }
    }
}

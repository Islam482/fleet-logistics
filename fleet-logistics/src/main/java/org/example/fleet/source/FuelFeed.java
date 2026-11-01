package org.example.fleet.source;

import org.example.fleet.events.FuelReadingEvent;
import org.example.fleet.observer.EventHub;

import java.util.function.Consumer;

public final class FuelFeed extends EventHub<FuelReadingEvent> {
    public FuelFeed(Consumer<RuntimeException> failureHandler) {
        super(failureHandler);
    }

    public void report(String vehicleId, double litersRemaining) {
        publish(new FuelReadingEvent(vehicleId, litersRemaining));
    }
}

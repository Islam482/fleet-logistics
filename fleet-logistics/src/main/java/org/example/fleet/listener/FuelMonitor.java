package org.example.fleet.listener;

import org.example.fleet.events.FuelAlertEvent;
import org.example.fleet.events.FuelReadingEvent;
import org.example.fleet.events.VehicleEvent;
import org.example.fleet.observer.EventHub;
import org.example.fleet.observer.Observer;

import java.util.concurrent.atomic.LongAdder;

public final class FuelMonitor implements Observer<FuelReadingEvent> {
    private final double lowFuelLiters;
    private final EventHub<VehicleEvent> alerts;
    private final LongAdder readings = new LongAdder();
    private final LongAdder lowFuelAlerts = new LongAdder();

    public FuelMonitor(double lowFuelLiters, EventHub<VehicleEvent> alerts) {
        this.lowFuelLiters = lowFuelLiters;
        this.alerts = alerts;
    }

    @Override
    public void onEvent(FuelReadingEvent event) {
        readings.increment();
        if (event.litersRemaining() < lowFuelLiters) {
            lowFuelAlerts.increment();
            alerts.publish(new FuelAlertEvent(event.vehicleId(), event.litersRemaining()));
        }
    }

    public long readings() {
        return readings.sum();
    }

    public long lowFuelAlerts() {
        return lowFuelAlerts.sum();
    }
}

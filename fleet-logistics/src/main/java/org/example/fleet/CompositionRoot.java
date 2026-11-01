package org.example.fleet;

import org.example.fleet.events.VehicleEvent;
import org.example.fleet.listener.AlertService;
import org.example.fleet.listener.DepotAggregator;
import org.example.fleet.listener.DepotDashboard;
import org.example.fleet.listener.EtaCalculator;
import org.example.fleet.listener.FuelMonitor;
import org.example.fleet.listener.GeofenceAlarm;
import org.example.fleet.listener.MapDisplay;
import org.example.fleet.listener.TicketLog;
import org.example.fleet.model.FleetDirectory;
import org.example.fleet.observer.EventHub;
import org.example.fleet.observer.EventType;
import org.example.fleet.sim.FleetSimulator;
import org.example.fleet.source.FuelFeed;
import org.example.fleet.source.GpsFeed;
import org.example.fleet.visitor.InspectionVisitor;
import org.example.fleet.visitor.MaintenanceCostVisitor;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class CompositionRoot {
    static final double LOW_FUEL_LITERS = 20.0;
    static final int SUMMARY_EVERY_EVENTS = 100;

    private CompositionRoot() {
    }

    public static FleetSystem assemble(FleetConfig config, FleetScenario scenario,
                                       Consumer<RuntimeException> failureHandler) {
        FleetDirectory directory = new FleetDirectory(scenario.fleet());

        GpsFeed gpsFeed = new GpsFeed(failureHandler);
        FuelFeed fuelFeed = new FuelFeed(failureHandler);
        EventHub<VehicleEvent> alerts = new EventHub<>(failureHandler);

        MapDisplay mapDisplay = new MapDisplay();
        EtaCalculator etaCalculator = new EtaCalculator(scenario.destinations());
        GeofenceAlarm geofenceAlarm = new GeofenceAlarm(scenario.zone(), alerts);
        FuelMonitor fuelMonitor = new FuelMonitor(LOW_FUEL_LITERS, alerts);
        DepotAggregator depotAggregator = new DepotAggregator(directory, SUMMARY_EVERY_EVENTS, failureHandler);
        DepotDashboard depotDashboard = new DepotDashboard();
        AlertService alertService = new AlertService(
                directory, new InspectionVisitor(), new MaintenanceCostVisitor(), failureHandler);
        TicketLog ticketLog = new TicketLog();

        gpsFeed.subscribe(EventType.GPS_POSITION, mapDisplay);
        gpsFeed.subscribe(EventType.GPS_POSITION, etaCalculator);
        gpsFeed.subscribe(EventType.GPS_POSITION, geofenceAlarm);
        gpsFeed.subscribe(EventType.GPS_POSITION, depotAggregator);
        fuelFeed.subscribe(EventType.FUEL_READING, fuelMonitor);
        fuelFeed.subscribe(EventType.FUEL_READING, depotAggregator);
        depotAggregator.subscribe(EventType.DEPOT_SUMMARY, depotDashboard);
        alerts.subscribe(EventType.GEOFENCE_ALERT, alertService);
        alerts.subscribe(EventType.FUEL_ALERT, alertService);
        alertService.subscribe(EventType.MAINTENANCE_TICKET, ticketLog);

        List<String> trackedVehicles = scenario.destinations().keySet().stream()
                .sorted(Comparator.naturalOrder()).toList();
        FleetSimulator simulator = new FleetSimulator(
                Executors.newFixedThreadPool(config.producerThreads(), producerThreads()),
                gpsFeed, fuelFeed, trackedVehicles, scenario.zone(), config);

        return new FleetSystem(directory, gpsFeed, fuelFeed, alerts, mapDisplay, etaCalculator,
                geofenceAlarm, fuelMonitor, depotAggregator, depotDashboard, alertService, ticketLog, simulator);
    }

    private static ThreadFactory producerThreads() {
        AtomicInteger counter = new AtomicInteger();
        return task -> new Thread(task, "producer-" + counter.incrementAndGet());
    }
}

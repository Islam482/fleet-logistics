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
import org.example.fleet.sim.FleetSimulator;
import org.example.fleet.source.FuelFeed;
import org.example.fleet.source.GpsFeed;

public record FleetSystem(
        FleetDirectory directory,
        GpsFeed gpsFeed,
        FuelFeed fuelFeed,
        EventHub<VehicleEvent> alerts,
        MapDisplay mapDisplay,
        EtaCalculator etaCalculator,
        GeofenceAlarm geofenceAlarm,
        FuelMonitor fuelMonitor,
        DepotAggregator depotAggregator,
        DepotDashboard depotDashboard,
        AlertService alertService,
        TicketLog ticketLog,
        FleetSimulator simulator) implements AutoCloseable {
    @Override
    public void close() {
        simulator.close();
    }
}

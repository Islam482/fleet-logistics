package org.example.fleet;

import org.example.fleet.events.MaintenanceTicketEvent;
import org.example.fleet.listener.AlertService;
import org.example.fleet.model.Depot;
import org.example.fleet.model.Fleet;
import org.example.fleet.model.FleetDirectory;
import org.example.fleet.model.GeoPoint;
import org.example.fleet.model.Truck;
import org.example.fleet.observer.EventType;
import org.example.fleet.visitor.InspectionVisitor;
import org.example.fleet.visitor.MaintenanceCostVisitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationTest {
    private FleetSystem system;

    @BeforeEach
    void assemble() {
        system = CompositionRoot.assemble(new FleetConfig(1, 0, 1), SampleData.scenario(), e -> { });
    }

    @AfterEach
    void shutDown() {
        system.close();
    }

    @Test
    void lowFuelInTheNorthInspectsOnlyTheNorthDepot() {
        system.fuelFeed().report("T1", 5.0);

        List<MaintenanceTicketEvent> tickets = system.ticketLog().tickets();
        assertEquals(1, tickets.size());
        MaintenanceTicketEvent ticket = tickets.get(0);
        assertEquals("D-NORTH", ticket.depotId());
        assertEquals("T1", ticket.vehicleId());
        assertEquals(EventType.FUEL_ALERT, ticket.trigger());
        assertEquals(List.of("T2", "DR1", "C1"), ticket.overdueAssetIds());
        assertEquals(1506.0, ticket.estimatedMonthlyCost(), 1e-9);
    }

    @Test
    void geofenceBreachInTheSouthInspectsOnlyTheSouthDepot() {
        system.gpsFeed().report("V3", new GeoPoint(0, 0), 40.0);

        MaintenanceTicketEvent ticket = system.ticketLog().tickets().get(0);
        assertEquals("D-SOUTH", ticket.depotId());
        assertEquals(EventType.GEOFENCE_ALERT, ticket.trigger());
        assertEquals(List.of("V2"), ticket.overdueAssetIds());
        assertEquals(1382.0, ticket.estimatedMonthlyCost(), 1e-9);
    }

    @Test
    void readingsAboveTheThresholdAndPositionsInsideTheZoneStartNothing() {
        system.fuelFeed().report("T1", 150.0);
        system.gpsFeed().report("T1", new GeoPoint(43.238, 76.945), 30.0);

        assertEquals(0, system.alertService().inspectionsRun());
        assertTrue(system.ticketLog().tickets().isEmpty());
    }

    @Test
    void alertForAnUnknownVehicleRunsNoInspection() {
        system.fuelFeed().report("GHOST", 1.0);

        assertEquals(0, system.alertService().inspectionsRun());
        assertTrue(system.ticketLog().tickets().isEmpty());
    }

    @Test
    void inspectionWithNothingOverdueCreatesNoTicket() {
        Depot tidy = new Depot("D-TIDY", "Tidy", List.of(new Truck("TT", 100, 10, 0.1, 1)));
        AlertService service = new AlertService(
                new FleetDirectory(new Fleet("F", "f", List.of(tidy))),
                new InspectionVisitor(), new MaintenanceCostVisitor(), e -> { });
        List<MaintenanceTicketEvent> tickets = new java.util.ArrayList<>();
        service.subscribe(EventType.MAINTENANCE_TICKET, tickets::add);

        service.onEvent(new org.example.fleet.events.FuelAlertEvent("TT", 1.0));

        assertEquals(1, service.inspectionsRun());
        assertTrue(tickets.isEmpty());
    }
}

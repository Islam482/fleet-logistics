package org.example.fleet.visitor;

import org.example.fleet.model.CargoBike;
import org.example.fleet.model.Depot;
import org.example.fleet.model.FleetElement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CargoBikeTest {
    private final Depot depot = new Depot("D", "City", List.of(new CargoBike("B1", 300, 0.05, 130)));

    @Test
    void maintenanceIsKmTimesRate() {
        assertEquals(15.0, depot.accept(new MaintenanceCostVisitor()), 1e-9);
    }

    @Test
    void usesNoFuelAndEmitsNothing() {
        Co2ReportVisitor co2 = new Co2ReportVisitor();

        assertEquals(0.0, depot.accept(new FuelCostVisitor(new FuelCostVisitor.Prices(1.5, 1.8, 0.2))), 1e-9);
        assertEquals(0.0, depot.accept(co2), 1e-9);
        assertEquals(0.0, co2.emissionsByType().get("CargoBike"), 1e-9);
    }

    @Test
    void isOverdueAfterOneHundredTwentyDays() {
        List<FleetElement> overdue = depot.accept(new InspectionVisitor());

        assertEquals(List.of("B1"), overdue.stream().map(FleetElement::id).toList());
    }

    @Test
    void isExportedAsJson() {
        assertEquals("{\"type\":\"depot\",\"id\":\"D\",\"name\":\"City\",\"assets\":[{\"type\":\"cargoBike\","
                + "\"id\":\"B1\",\"monthlyKm\":300.0,\"maintenancePerKm\":0.05,\"daysSinceInspection\":130}]}",
                depot.accept(new JsonExportVisitor()));
    }
}

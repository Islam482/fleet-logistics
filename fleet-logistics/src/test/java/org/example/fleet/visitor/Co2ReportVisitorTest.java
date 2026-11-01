package org.example.fleet.visitor;

import org.example.fleet.SampleData;
import org.example.fleet.model.Fleet;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Co2ReportVisitorTest {
    private final Fleet fleet = SampleData.scenario().fleet();

    @Test
    void fleetTotalIsTheSumOfAllTypes() {
        assertEquals(12526.9, fleet.accept(new Co2ReportVisitor()), 1e-6);
    }

    @Test
    void stateCollectedDuringTheWalkGivesTheReportPerType() {
        Co2ReportVisitor visitor = new Co2ReportVisitor();
        fleet.accept(visitor);

        Map<String, Double> report = visitor.emissionsByType();
        assertEquals(10934.4, report.get("Truck"), 1e-6);
        assertEquals(1547.7, report.get("Van"), 1e-6);
        assertEquals(44.8, report.get("Drone"), 1e-6);
        assertEquals(0.0, report.get("ChargingStation"), 1e-9);
        assertEquals(4, report.size());
    }

    @Test
    void oneVisitorAccumulatesAcrossSeveralSubtrees() {
        Co2ReportVisitor visitor = new Co2ReportVisitor();
        double north = fleet.depots().get(0).accept(visitor);
        double south = fleet.depots().get(1).accept(visitor);

        assertEquals(6789.75, north, 1e-6);
        assertEquals(12526.9, north + south, 1e-6);
        assertEquals(10934.4, visitor.emissionsByType().get("Truck"), 1e-6);
    }

    @Test
    void subtreeVisitOnlyCountsThatSubtree() {
        Co2ReportVisitor visitor = new Co2ReportVisitor();
        fleet.depots().get(0).accept(visitor);

        assertEquals(6244.4, visitor.emissionsByType().get("Truck"), 1e-6);
    }

    @Test
    void returnedReportCannotBeModified() {
        Co2ReportVisitor visitor = new Co2ReportVisitor();
        fleet.accept(visitor);

        assertThrows(UnsupportedOperationException.class, () -> visitor.emissionsByType().put("Truck", 0.0));
    }
}

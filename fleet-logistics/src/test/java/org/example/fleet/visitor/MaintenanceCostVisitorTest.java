package org.example.fleet.visitor;

import org.example.fleet.SampleData;
import org.example.fleet.model.Depot;
import org.example.fleet.model.Fleet;
import org.example.fleet.model.Truck;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaintenanceCostVisitorTest {
    private final Fleet fleet = SampleData.scenario().fleet();
    private final MaintenanceCostVisitor visitor = new MaintenanceCostVisitor();

    @Test
    void singleLeafUsesItsOwnFormula() {
        Truck t1 = (Truck) fleet.depots().get(0).assets().get(0);
        assertEquals(600.0, t1.accept(visitor), 1e-9);
    }

    @Test
    void depotIsTheSumOfItsAssets() {
        Depot north = fleet.depots().get(0);
        Depot south = fleet.depots().get(1);

        assertEquals(1506.0, north.accept(visitor), 1e-9);
        assertEquals(1382.0, south.accept(visitor), 1e-9);
    }

    @Test
    void fleetIsTheSumOfItsDepots() {
        assertEquals(2888.0, fleet.accept(visitor), 1e-9);
    }

    @Test
    void emptyDepotCostsNothing() {
        assertEquals(0.0, new Depot("D", "empty", java.util.List.of()).accept(visitor), 1e-9);
    }
}

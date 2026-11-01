package org.example.fleet.visitor;

import org.example.fleet.SampleData;
import org.example.fleet.model.Fleet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FuelCostVisitorTest {
    private final Fleet fleet = SampleData.scenario().fleet();
    private final FuelCostVisitor visitor = new FuelCostVisitor(new FuelCostVisitor.Prices(1.50, 1.80, 0.20));

    @Test
    void depotsAreCalculatedFromTheirOwnAssets() {
        assertEquals(3912.8, fleet.depots().get(0).accept(visitor), 1e-9);
        assertEquals(3435.6, fleet.depots().get(1).accept(visitor), 1e-9);
    }

    @Test
    void fleetIsTheSumOfBothDepots() {
        assertEquals(7348.4, fleet.accept(visitor), 1e-9);
    }
}

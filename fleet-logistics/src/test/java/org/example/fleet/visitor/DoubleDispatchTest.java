package org.example.fleet.visitor;

import org.example.fleet.SampleData;
import org.example.fleet.model.CargoBike;
import org.example.fleet.model.ChargingStation;
import org.example.fleet.model.Depot;
import org.example.fleet.model.Drone;
import org.example.fleet.model.Fleet;
import org.example.fleet.model.FleetElement;
import org.example.fleet.model.Truck;
import org.example.fleet.model.Van;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoubleDispatchTest {
    private static final class NameVisitor implements FleetVisitor<String> {
        public String visit(Fleet fleet) { return "fleet"; }
        public String visit(Depot depot) { return "depot"; }
        public String visit(Truck truck) { return "truck"; }
        public String visit(Van van) { return "van"; }
        public String visit(Drone drone) { return "drone"; }
        public String visit(CargoBike bike) { return "bike"; }
        public String visit(ChargingStation station) { return "charger"; }
    }

    @Test
    void everyNodeSelectsItsOwnOverloadEvenWhenHeldAsTheInterfaceType() {
        List<FleetElement> nodes = List.of(
                new Truck("t", 1, 1, 1, 1), new Van("v", 1, 1, 1, 1), new Drone("d", 1, 1, 1, 1),
                new CargoBike("b", 1, 1, 1), new ChargingStation("c", 1, 1), new Depot("x", "x", List.of()),
                new Fleet("f", "f", List.of()));

        List<String> chosen = new ArrayList<>();
        for (FleetElement node : nodes) {
            chosen.add(node.accept(new NameVisitor()));
        }

        assertEquals(List.of("truck", "van", "drone", "bike", "charger", "depot", "fleet"), chosen);
    }

    @Test
    void sameTreeGivesDifferentResultTypesPerVisitor() {
        Fleet fleet = SampleData.scenario().fleet();

        Double number = fleet.accept(new MaintenanceCostVisitor());
        String text = fleet.accept(new NameVisitor());

        assertEquals(2888.0, number, 1e-9);
        assertEquals("fleet", text);
    }
}

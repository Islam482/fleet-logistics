package org.example.fleet.visitor;

import org.example.fleet.SampleData;
import org.example.fleet.model.Depot;
import org.example.fleet.model.Fleet;
import org.example.fleet.model.FleetElement;
import org.example.fleet.model.Truck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InspectionVisitorTest {
    private final Fleet fleet = SampleData.scenario().fleet();
    private final InspectionVisitor visitor = new InspectionVisitor();

    private static List<String> ids(List<FleetElement> elements) {
        return elements.stream().map(FleetElement::id).toList();
    }

    @Test
    void wholeFleetListsEveryOverdueAssetInTreeOrder() {
        assertEquals(List.of("T2", "DR1", "C1", "V2"), ids(fleet.accept(visitor)));
    }

    @Test
    void depotSubtreeListsOnlyItsOwnAssets() {
        assertEquals(List.of("T2", "DR1", "C1"), ids(fleet.depots().get(0).accept(visitor)));
        assertEquals(List.of("V2"), ids(fleet.depots().get(1).accept(visitor)));
    }

    @Test
    void limitIsExclusive() {
        Depot depot = new Depot("D", "d", List.of(
                new Truck("AT", 1, 1, 1, 90),
                new Truck("OVER", 1, 1, 1, 91)));

        assertEquals(List.of("OVER"), ids(depot.accept(visitor)));
    }

    @Test
    void returnsTheElementsThemselvesNotCopies() {
        Truck overdue = new Truck("T", 1, 1, 1, 200);
        List<FleetElement> found = new Depot("D", "d", List.of(overdue)).accept(visitor);

        assertEquals(overdue, found.get(0));
    }
}

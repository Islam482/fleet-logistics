package org.example.fleet.visitor;

import org.example.fleet.SampleData;
import org.example.fleet.model.ChargingStation;
import org.example.fleet.model.Depot;
import org.example.fleet.model.Fleet;
import org.example.fleet.model.Truck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonExportVisitorTest {
    private final JsonExportVisitor visitor = new JsonExportVisitor();

    @Test
    void exportsNestedStructureExactly() {
        Fleet mini = new Fleet("F", "Mini", List.of(
                new Depot("D", "Main", List.of(
                        new Truck("T", 100, 10, 0.5, 5),
                        new ChargingStation("C", 50, 7)))));

        String expected = "{\"type\":\"fleet\",\"id\":\"F\",\"name\":\"Mini\",\"depots\":["
                + "{\"type\":\"depot\",\"id\":\"D\",\"name\":\"Main\",\"assets\":["
                + "{\"type\":\"truck\",\"id\":\"T\",\"monthlyKm\":100.0,\"litersPer100Km\":10.0,"
                + "\"maintenancePerKm\":0.5,\"daysSinceInspection\":5},"
                + "{\"type\":\"chargingStation\",\"id\":\"C\",\"monthlyServiceFee\":50.0,\"daysSinceInspection\":7}"
                + "]}]}";

        assertEquals(expected, mini.accept(visitor));
    }

    @Test
    void emptyContainerExportsAnEmptyArray() {
        assertEquals("{\"type\":\"depot\",\"id\":\"D\",\"name\":\"n\",\"assets\":[]}",
                new Depot("D", "n", List.of()).accept(visitor));
    }

    @Test
    void escapesQuotesBackslashesAndControlCharacters() {
        Depot depot = new Depot("D", "A \"q\" \\ line\nbreak\u0001", List.of());

        assertTrue(depot.accept(visitor).contains("\"name\":\"A \\\"q\\\" \\\\ line\\nbreak\\u0001\""));
    }

    @Test
    void sampleFleetKeepsBracketsBalancedAndContainsEveryAssetOnce() {
        String json = SampleData.scenario().fleet().accept(visitor);

        assertEquals(count(json, '{'), count(json, '}'));
        assertEquals(count(json, '['), count(json, ']'));

        assertEquals(13, count(json, '{'));
        assertEquals(3, count(json, '['));
        for (String id : List.of("T1", "T2", "V1", "DR1", "C1", "T3", "V2", "V3", "DR2", "C2")) {
            assertEquals(1, occurrences(json, "\"id\":\"" + id + "\""), id);
        }
    }

    @Test
    void depotsAreNestedInsideTheFleetArray() {
        String json = SampleData.scenario().fleet().accept(visitor);

        int depotsArray = json.indexOf("\"depots\":[");
        assertTrue(depotsArray > 0);
        assertTrue(json.indexOf("\"id\":\"D-NORTH\"") > depotsArray);
        assertTrue(json.indexOf("\"id\":\"D-SOUTH\"") > json.indexOf("\"id\":\"D-NORTH\""));
        assertTrue(json.endsWith("]}]}"));
    }

    private static int count(String text, char c) {
        return (int) text.chars().filter(x -> x == c).count();
    }

    private static int occurrences(String text, String part) {
        int count = 0;
        for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + 1)) {
            count++;
        }
        return count;
    }
}

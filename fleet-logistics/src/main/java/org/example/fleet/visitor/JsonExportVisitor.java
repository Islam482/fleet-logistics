package org.example.fleet.visitor;

import org.example.fleet.model.CargoBike;
import org.example.fleet.model.ChargingStation;
import org.example.fleet.model.Depot;
import org.example.fleet.model.Drone;
import org.example.fleet.model.Fleet;
import org.example.fleet.model.FleetElement;
import org.example.fleet.model.Truck;
import org.example.fleet.model.Van;

import java.util.List;
import java.util.stream.Collectors;

public final class JsonExportVisitor implements FleetVisitor<String> {
    @Override
    public String visit(Fleet fleet) {
        return object(
                string("type", "fleet"), string("id", fleet.id()), string("name", fleet.name()),
                array("depots", fleet.depots()));
    }

    @Override
    public String visit(Depot depot) {
        return object(
                string("type", "depot"), string("id", depot.id()), string("name", depot.name()),
                array("assets", depot.assets()));
    }

    @Override
    public String visit(Truck truck) {
        return object(
                string("type", "truck"), string("id", truck.id()),
                number("monthlyKm", truck.monthlyKm()), number("litersPer100Km", truck.litersPer100Km()),
                number("maintenancePerKm", truck.maintenancePerKm()),
                number("daysSinceInspection", truck.daysSinceInspection()));
    }

    @Override
    public String visit(Van van) {
        return object(
                string("type", "van"), string("id", van.id()),
                number("monthlyKm", van.monthlyKm()), number("litersPer100Km", van.litersPer100Km()),
                number("maintenancePerKm", van.maintenancePerKm()),
                number("daysSinceInspection", van.daysSinceInspection()));
    }

    @Override
    public String visit(Drone drone) {
        return object(
                string("type", "drone"), string("id", drone.id()),
                number("monthlyFlightHours", drone.monthlyFlightHours()), number("kwhPerHour", drone.kwhPerHour()),
                number("maintenancePerHour", drone.maintenancePerHour()),
                number("daysSinceInspection", drone.daysSinceInspection()));
    }

    @Override
    public String visit(CargoBike bike) {
        return object(
                string("type", "cargoBike"), string("id", bike.id()),
                number("monthlyKm", bike.monthlyKm()), number("maintenancePerKm", bike.maintenancePerKm()),
                number("daysSinceInspection", bike.daysSinceInspection()));
    }

    @Override
    public String visit(ChargingStation station) {
        return object(
                string("type", "chargingStation"), string("id", station.id()),
                number("monthlyServiceFee", station.monthlyServiceFee()),
                number("daysSinceInspection", station.daysSinceInspection()));
    }

    private static String object(String... members) {
        return "{" + String.join(",", members) + "}";
    }

    private String array(String name, List<? extends FleetElement> children) {
        String items = children.stream().map(child -> child.accept(this)).collect(Collectors.joining(","));
        return quote(name) + ":[" + items + "]";
    }

    private static String string(String name, String value) {
        return quote(name) + ":" + quote(value);
    }

    private static String number(String name, double value) {
        return quote(name) + ":" + value;
    }

    private static String number(String name, long value) {
        return quote(name) + ":" + value;
    }

    private static String quote(String text) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}

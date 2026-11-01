package org.example.fleet.visitor;

import org.example.fleet.model.CargoBike;
import org.example.fleet.model.ChargingStation;
import org.example.fleet.model.Drone;
import org.example.fleet.model.FleetElement;
import org.example.fleet.model.Truck;
import org.example.fleet.model.Van;

import java.util.ArrayList;
import java.util.List;

public final class InspectionVisitor extends FoldingVisitor<List<FleetElement>> {
    private static final int TRUCK_LIMIT_DAYS = 90;
    private static final int VAN_LIMIT_DAYS = 120;
    private static final int DRONE_LIMIT_DAYS = 60;
    private static final int CHARGER_LIMIT_DAYS = 180;
    private static final int BIKE_LIMIT_DAYS = 120;

    @Override
    protected List<FleetElement> neutral() {
        return List.of();
    }

    @Override
    protected List<FleetElement> combine(List<FleetElement> left, List<FleetElement> right) {
        List<FleetElement> merged = new ArrayList<>(left);
        merged.addAll(right);
        return merged;
    }

    @Override
    public List<FleetElement> visit(Truck truck) {
        return overdueIf(truck, truck.daysSinceInspection() > TRUCK_LIMIT_DAYS);
    }

    @Override
    public List<FleetElement> visit(Van van) {
        return overdueIf(van, van.daysSinceInspection() > VAN_LIMIT_DAYS);
    }

    @Override
    public List<FleetElement> visit(Drone drone) {
        return overdueIf(drone, drone.daysSinceInspection() > DRONE_LIMIT_DAYS);
    }

    @Override
    public List<FleetElement> visit(CargoBike bike) {
        return overdueIf(bike, bike.daysSinceInspection() > BIKE_LIMIT_DAYS);
    }

    @Override
    public List<FleetElement> visit(ChargingStation station) {
        return overdueIf(station, station.daysSinceInspection() > CHARGER_LIMIT_DAYS);
    }

    private static List<FleetElement> overdueIf(FleetElement element, boolean overdue) {
        return overdue ? List.of(element) : List.of();
    }
}

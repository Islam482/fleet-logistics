package org.example.fleet.visitor;

import org.example.fleet.model.CargoBike;
import org.example.fleet.model.ChargingStation;
import org.example.fleet.model.Drone;
import org.example.fleet.model.Truck;
import org.example.fleet.model.Van;

public final class MaintenanceCostVisitor extends FoldingVisitor<Double> {
    @Override
    protected Double neutral() {
        return 0.0;
    }

    @Override
    protected Double combine(Double left, Double right) {
        return left + right;
    }

    @Override
    public Double visit(Truck truck) {
        return truck.monthlyKm() * truck.maintenancePerKm();
    }

    @Override
    public Double visit(Van van) {
        return van.monthlyKm() * van.maintenancePerKm();
    }

    @Override
    public Double visit(Drone drone) {
        return drone.monthlyFlightHours() * drone.maintenancePerHour();
    }

    @Override
    public Double visit(CargoBike bike) {
        return bike.monthlyKm() * bike.maintenancePerKm();
    }

    @Override
    public Double visit(ChargingStation station) {
        return station.monthlyServiceFee();
    }
}

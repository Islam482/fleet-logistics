package org.example.fleet.visitor;

import org.example.fleet.model.CargoBike;
import org.example.fleet.model.ChargingStation;
import org.example.fleet.model.Depot;
import org.example.fleet.model.Drone;
import org.example.fleet.model.Fleet;
import org.example.fleet.model.Truck;
import org.example.fleet.model.Van;

public interface FleetVisitor<R> {
    R visit(Fleet fleet);

    R visit(Depot depot);

    R visit(Truck truck);

    R visit(Van van);

    R visit(Drone drone);

    R visit(CargoBike bike);

    R visit(ChargingStation station);
}

package org.example.fleet.model;

import org.example.fleet.visitor.FleetVisitor;

public record ChargingStation(String id, double monthlyServiceFee, int daysSinceInspection)
        implements FleetElement {
    @Override
    public <R> R accept(FleetVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

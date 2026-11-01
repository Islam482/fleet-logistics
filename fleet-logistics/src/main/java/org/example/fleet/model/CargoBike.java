package org.example.fleet.model;

import org.example.fleet.visitor.FleetVisitor;

public record CargoBike(String id, double monthlyKm, double maintenancePerKm, int daysSinceInspection)
        implements FleetElement {
    @Override
    public <R> R accept(FleetVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

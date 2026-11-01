package org.example.fleet.model;

import org.example.fleet.visitor.FleetVisitor;

public record Van(String id, double monthlyKm, double litersPer100Km, double maintenancePerKm,
                  int daysSinceInspection) implements FleetElement {
    @Override
    public <R> R accept(FleetVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

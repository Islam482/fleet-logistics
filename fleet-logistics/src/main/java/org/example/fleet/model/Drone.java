package org.example.fleet.model;

import org.example.fleet.visitor.FleetVisitor;

public record Drone(String id, double monthlyFlightHours, double kwhPerHour, double maintenancePerHour,
                    int daysSinceInspection) implements FleetElement {
    @Override
    public <R> R accept(FleetVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

package org.example.fleet.model;

import org.example.fleet.visitor.FleetVisitor;

import java.util.List;

public record Depot(String id, String name, List<FleetElement> assets) implements FleetElement {
    public Depot {
        assets = List.copyOf(assets);
    }

    @Override
    public <R> R accept(FleetVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

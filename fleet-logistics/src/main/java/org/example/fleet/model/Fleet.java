package org.example.fleet.model;

import org.example.fleet.visitor.FleetVisitor;

import java.util.List;

public record Fleet(String id, String name, List<Depot> depots) implements FleetElement {
    public Fleet {
        depots = List.copyOf(depots);
    }

    @Override
    public <R> R accept(FleetVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

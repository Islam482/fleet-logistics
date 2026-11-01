package org.example.fleet.model;

import org.example.fleet.visitor.FleetVisitor;

public interface FleetElement {
    String id();

    <R> R accept(FleetVisitor<R> visitor);
}

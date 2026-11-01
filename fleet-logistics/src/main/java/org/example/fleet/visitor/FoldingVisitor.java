package org.example.fleet.visitor;

import org.example.fleet.model.Depot;
import org.example.fleet.model.Fleet;
import org.example.fleet.model.FleetElement;

import java.util.List;

public abstract class FoldingVisitor<R> implements FleetVisitor<R> {
    protected abstract R neutral();

    protected abstract R combine(R left, R right);

    @Override
    public R visit(Fleet fleet) {
        return foldChildren(fleet.depots());
    }

    @Override
    public R visit(Depot depot) {
        return foldChildren(depot.assets());
    }

    private R foldChildren(List<? extends FleetElement> children) {
        R result = neutral();
        for (FleetElement child : children) {
            result = combine(result, child.accept(this));
        }
        return result;
    }
}

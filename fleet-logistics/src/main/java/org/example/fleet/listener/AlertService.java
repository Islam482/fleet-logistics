package org.example.fleet.listener;

import org.example.fleet.events.MaintenanceTicketEvent;
import org.example.fleet.events.VehicleEvent;
import org.example.fleet.model.Depot;
import org.example.fleet.model.FleetDirectory;
import org.example.fleet.model.FleetElement;
import org.example.fleet.observer.EventHub;
import org.example.fleet.observer.Observer;
import org.example.fleet.visitor.InspectionVisitor;
import org.example.fleet.visitor.MaintenanceCostVisitor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

public final class AlertService extends EventHub<MaintenanceTicketEvent> implements Observer<VehicleEvent> {
    private final FleetDirectory directory;
    private final InspectionVisitor inspection;
    private final MaintenanceCostVisitor maintenanceCost;
    private final LongAdder inspectionsRun = new LongAdder();

    public AlertService(FleetDirectory directory, InspectionVisitor inspection,
                        MaintenanceCostVisitor maintenanceCost, Consumer<RuntimeException> failureHandler) {
        super(failureHandler);
        this.directory = directory;
        this.inspection = inspection;
        this.maintenanceCost = maintenanceCost;
    }

    @Override
    public void onEvent(VehicleEvent alert) {
        Optional<Depot> depot = directory.depotOf(alert.vehicleId());
        if (depot.isEmpty()) {
            return;
        }
        List<FleetElement> overdue = depot.get().accept(inspection);
        inspectionsRun.increment();
        if (overdue.isEmpty()) {
            return;
        }
        double monthlyCost = depot.get().accept(maintenanceCost);
        List<String> overdueIds = overdue.stream().map(FleetElement::id).toList();
        publish(new MaintenanceTicketEvent(alert.vehicleId(), depot.get().id(), alert.type(), overdueIds, monthlyCost));
    }

    public long inspectionsRun() {
        return inspectionsRun.sum();
    }
}

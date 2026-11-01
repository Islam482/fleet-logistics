package org.example.fleet;

import org.example.fleet.visitor.Co2ReportVisitor;
import org.example.fleet.visitor.InspectionVisitor;
import org.example.fleet.visitor.JsonExportVisitor;
import org.example.fleet.visitor.MaintenanceCostVisitor;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        FleetConfig config = new FleetConfig(intArg(args, 0, 4), intArg(args, 1, 1000), intArg(args, 2, 42));
        FleetScenario scenario = SampleData.scenario();

        try (FleetSystem system = CompositionRoot.assemble(config, scenario,
                failure -> System.err.println("Listener failed: " + failure))) {
            system.simulator().run();

            System.out.println("== Observer side ==");
            System.out.println("GPS updates on map      : " + system.mapDisplay().updates());
            System.out.println("Fuel readings           : " + system.fuelMonitor().readings());
            System.out.println("Geofence breaches       : " + system.geofenceAlarm().breaches());
            System.out.println("Low-fuel alerts         : " + system.fuelMonitor().lowFuelAlerts());
            System.out.println("Depot summaries shown   : " + system.depotDashboard().received());
            System.out.println("Inspections run         : " + system.alertService().inspectionsRun());
            System.out.println("Maintenance tickets     : " + system.ticketLog().tickets().size());

            System.out.println("== Visitor side ==");
            Co2ReportVisitor co2 = new Co2ReportVisitor();
            double totalCo2 = scenario.fleet().accept(co2);
            System.out.println("Maintenance cost / month: " + scenario.fleet().accept(new MaintenanceCostVisitor()));
            System.out.println("CO2 kg / month          : " + totalCo2 + " " + co2.emissionsByType());
            System.out.println("Inspections overdue     : " + scenario.fleet().accept(new InspectionVisitor()));
            System.out.println("JSON export             : " + scenario.fleet().accept(new JsonExportVisitor()));
        }
    }

    private static int intArg(String[] args, int index, int fallback) {
        return args.length > index ? Integer.parseInt(args[index]) : fallback;
    }
}

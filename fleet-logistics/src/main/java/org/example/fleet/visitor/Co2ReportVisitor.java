package org.example.fleet.visitor;

import org.example.fleet.model.CargoBike;
import org.example.fleet.model.ChargingStation;
import org.example.fleet.model.Drone;
import org.example.fleet.model.Truck;
import org.example.fleet.model.Van;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public final class Co2ReportVisitor extends FoldingVisitor<Double> {
    private static final double DIESEL_KG_PER_LITER = 2.68;
    private static final double PETROL_KG_PER_LITER = 2.31;
    private static final double GRID_KG_PER_KWH = 0.4;

    private final Map<String, Double> kgByType = new TreeMap<>();

    public Map<String, Double> emissionsByType() {
        return Collections.unmodifiableMap(new TreeMap<>(kgByType));
    }

    @Override
    protected Double neutral() {
        return 0.0;
    }

    @Override
    protected Double combine(Double left, Double right) {
        return left + right;
    }

    @Override
    public Double visit(Truck truck) {
        return record("Truck", Consumption.liters(truck.monthlyKm(), truck.litersPer100Km()) * DIESEL_KG_PER_LITER);
    }

    @Override
    public Double visit(Van van) {
        return record("Van", Consumption.liters(van.monthlyKm(), van.litersPer100Km()) * PETROL_KG_PER_LITER);
    }

    @Override
    public Double visit(Drone drone) {
        return record("Drone", drone.monthlyFlightHours() * drone.kwhPerHour() * GRID_KG_PER_KWH);
    }

    @Override
    public Double visit(CargoBike bike) {
        return record("CargoBike", 0.0);
    }

    @Override
    public Double visit(ChargingStation station) {
        return record("ChargingStation", 0.0);
    }

    private double record(String type, double kg) {
        kgByType.merge(type, kg, Double::sum);
        return kg;
    }
}

package org.example.fleet.visitor;

import org.example.fleet.model.CargoBike;
import org.example.fleet.model.ChargingStation;
import org.example.fleet.model.Drone;
import org.example.fleet.model.Truck;
import org.example.fleet.model.Van;

public final class FuelCostVisitor extends FoldingVisitor<Double> {
    public record Prices(double dieselPerLiter, double petrolPerLiter, double electricityPerKwh) {
    }

    private final Prices prices;

    public FuelCostVisitor(Prices prices) {
        this.prices = prices;
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
        return Consumption.liters(truck.monthlyKm(), truck.litersPer100Km()) * prices.dieselPerLiter();
    }

    @Override
    public Double visit(Van van) {
        return Consumption.liters(van.monthlyKm(), van.litersPer100Km()) * prices.petrolPerLiter();
    }

    @Override
    public Double visit(Drone drone) {
        return drone.monthlyFlightHours() * drone.kwhPerHour() * prices.electricityPerKwh();
    }

    @Override
    public Double visit(CargoBike bike) {
        return 0.0;
    }

    @Override
    public Double visit(ChargingStation station) {
        return 0.0;
    }
}

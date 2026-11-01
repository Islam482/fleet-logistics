package org.example.fleet.visitor;

final class Consumption {
    private Consumption() {
    }

    static double liters(double km, double litersPer100Km) {
        return km / 100.0 * litersPer100Km;
    }
}

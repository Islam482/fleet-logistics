package org.example.fleet.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class FleetDirectory {
    private final List<Depot> depots;
    private final Map<String, Depot> depotByAssetId = new HashMap<>();

    public FleetDirectory(Fleet fleet) {
        this.depots = fleet.depots();
        for (Depot depot : depots) {
            for (FleetElement asset : depot.assets()) {
                if (depotByAssetId.put(asset.id(), depot) != null) {
                    throw new IllegalArgumentException("Duplicate asset id: " + asset.id());
                }
            }
        }
    }

    public Optional<Depot> depotOf(String assetId) {
        return Optional.ofNullable(depotByAssetId.get(assetId));
    }

    public List<Depot> depots() {
        return depots;
    }
}

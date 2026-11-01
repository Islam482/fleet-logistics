package org.example.fleet.listener;

import org.example.fleet.events.DepotSummaryEvent;
import org.example.fleet.observer.Observer;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class DepotDashboard implements Observer<DepotSummaryEvent> {
    private final Map<String, DepotSummaryEvent> newest = new ConcurrentHashMap<>();
    private final LongAdder received = new LongAdder();

    @Override
    public void onEvent(DepotSummaryEvent event) {
        received.increment();

        newest.merge(event.depotId(), event,
                (current, candidate) -> candidate.eventCount() >= current.eventCount() ? candidate : current);
    }

    public Optional<DepotSummaryEvent> newestOf(String depotId) {
        return Optional.ofNullable(newest.get(depotId));
    }

    public long received() {
        return received.sum();
    }
}

package org.example.fleet.listener;

import org.example.fleet.events.MaintenanceTicketEvent;
import org.example.fleet.observer.Observer;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class TicketLog implements Observer<MaintenanceTicketEvent> {
    private final Queue<MaintenanceTicketEvent> tickets = new ConcurrentLinkedQueue<>();

    @Override
    public void onEvent(MaintenanceTicketEvent ticket) {
        tickets.add(ticket);
    }

    public List<MaintenanceTicketEvent> tickets() {
        return List.copyOf(tickets);
    }
}

package org.example.fleet.observer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventHub<E extends Event> implements Subject<E> {
    private final Map<EventType, CopyOnWriteArrayList<Observer<? super E>>> registry =
            new EnumMap<>(EventType.class);
    private final Consumer<RuntimeException> failureHandler;

    public EventHub(Consumer<RuntimeException> failureHandler) {
        this.failureHandler = Objects.requireNonNull(failureHandler);
        for (EventType type : EventType.values()) {
            registry.put(type, new CopyOnWriteArrayList<>());
        }
    }

    @Override
    public void subscribe(EventType type, Observer<? super E> observer) {
        registry.get(type).addIfAbsent(Objects.requireNonNull(observer));
    }

    @Override
    public void unsubscribe(EventType type, Observer<? super E> observer) {
        registry.get(type).remove(observer);
    }

    public void publish(E event) {
        for (Observer<? super E> observer : registry.get(event.type())) {
            try {
                observer.onEvent(event);
            } catch (RuntimeException failure) {
                failureHandler.accept(failure);
            }
        }
    }

    public int observerCount(EventType type) {
        return registry.get(type).size();
    }
}

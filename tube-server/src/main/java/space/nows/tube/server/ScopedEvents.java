package space.nows.tube.server;

import space.nows.tube.api.event.Event;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class ScopedEvents implements EventBus {

    private final EventBus delegate;
    private final List<Subscription> subscriptions = new ArrayList<>();

    ScopedEvents(EventBus delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized <E extends Event> Subscription subscribe(Class<E> type, EventPriority priority, boolean ignoreCancelled, Consumer<? super E> listener) {
        Subscription subscription = delegate.subscribe(type, priority, ignoreCancelled, listener);
        subscriptions.add(subscription);
        return subscription;
    }

    @Override
    public <E extends Event> E post(E event) {
        return delegate.post(event);
    }

    synchronized void cancelAll() {
        subscriptions.forEach(Subscription::cancel);
        subscriptions.clear();
    }
}

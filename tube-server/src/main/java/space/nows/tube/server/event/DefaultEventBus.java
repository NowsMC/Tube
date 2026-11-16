package space.nows.tube.server.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.event.Cancellable;
import space.nows.tube.api.event.Event;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.Subscription;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class DefaultEventBus implements EventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private record Registration(Class<? extends Event> type, EventPriority priority, boolean ignoreCancelled, Consumer<Event> listener) {
    }

    private final List<Registration> registrations = new CopyOnWriteArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Event> Subscription subscribe(Class<E> type, EventPriority priority, boolean ignoreCancelled, Consumer<? super E> listener) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(priority, "priority");
        Objects.requireNonNull(listener, "listener");
        Registration registration = new Registration(type, priority, ignoreCancelled, (Consumer<Event>) listener);
        synchronized (registrations) {
            int index = registrations.size();
            while (index > 0 && registrations.get(index - 1).priority().compareTo(priority) > 0) {
                index--;
            }
            registrations.add(index, registration);
        }
        return () -> registrations.remove(registration);
    }

    @Override
    public <E extends Event> E post(E event) {
        Objects.requireNonNull(event, "event");
        for (Registration registration : registrations) {
            if (!registration.type().isInstance(event)) {
                continue;
            }
            if (registration.ignoreCancelled() && event instanceof Cancellable cancellable && cancellable.isCancelled()) {
                continue;
            }
            try {
                registration.listener().accept(event);
            } catch (RuntimeException e) {
                LOGGER.error("Listener failed for {}", event.getClass().getSimpleName(), e);
            }
        }
        return event;
    }
}

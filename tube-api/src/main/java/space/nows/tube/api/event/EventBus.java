package space.nows.tube.api.event;

import java.util.function.Consumer;

public interface EventBus {

    <E extends Event> Subscription subscribe(Class<E> type, EventPriority priority, boolean ignoreCancelled, Consumer<? super E> listener);

    default <E extends Event> Subscription subscribe(Class<E> type, Consumer<? super E> listener) {
        return subscribe(type, EventPriority.NORMAL, false, listener);
    }

    <E extends Event> E post(E event);
}

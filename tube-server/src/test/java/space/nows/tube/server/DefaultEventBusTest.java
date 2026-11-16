package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import space.nows.tube.api.event.Cancellable;
import space.nows.tube.api.event.Event;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.Subscription;

import java.util.ArrayList;
import java.util.List;

class DefaultEventBusTest {

    private static class Base implements Event {
    }

    private static final class Derived extends Base {
    }

    private static final class Stoppable implements Cancellable {

        private boolean cancelled;

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    private final DefaultEventBus bus = new DefaultEventBus();
    private final List<String> calls = new ArrayList<>();

    @Test
    void runsListenersFromLowestToMonitorPriority() {
        bus.subscribe(Base.class, EventPriority.MONITOR, false, event -> calls.add("monitor"));
        bus.subscribe(Base.class, EventPriority.HIGH, false, event -> calls.add("high"));
        bus.subscribe(Base.class, EventPriority.LOWEST, false, event -> calls.add("lowest"));
        bus.subscribe(Base.class, EventPriority.HIGH, false, event -> calls.add("high-second"));

        bus.post(new Base());

        assertEquals(List.of("lowest", "high", "high-second", "monitor"), calls);
    }

    @Test
    void deliversSubclassEventsToSuperclassListeners() {
        bus.subscribe(Base.class, event -> calls.add("base"));
        bus.subscribe(Derived.class, event -> calls.add("derived"));

        bus.post(new Base());
        bus.post(new Derived());

        assertEquals(List.of("base", "base", "derived"), calls);
    }

    @Test
    void skipsCancelledEventsForListenersThatIgnoreThem() {
        bus.subscribe(Stoppable.class, EventPriority.LOW, false, event -> event.setCancelled(true));
        bus.subscribe(Stoppable.class, EventPriority.NORMAL, true, event -> calls.add("ignoring"));
        bus.subscribe(Stoppable.class, EventPriority.NORMAL, false, event -> calls.add("seeing"));

        Stoppable event = bus.post(new Stoppable());

        assertTrue(event.isCancelled());
        assertEquals(List.of("seeing"), calls);
    }

    @Test
    void stopsDeliveringAfterCancel() {
        Subscription subscription = bus.subscribe(Base.class, event -> calls.add("called"));
        bus.post(new Base());
        subscription.cancel();
        bus.post(new Base());
        assertEquals(List.of("called"), calls);
    }

    @Test
    void keepsRunningAfterAListenerThrows() {
        bus.subscribe(Base.class, EventPriority.LOW, false, event -> {
            throw new IllegalStateException("boom");
        });
        bus.subscribe(Base.class, EventPriority.HIGH, false, event -> calls.add("after"));

        bus.post(new Base());

        assertEquals(List.of("after"), calls);
    }
}

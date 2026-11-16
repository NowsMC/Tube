package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

class TickWatchdogTest {

    @Test
    void stallsOnlyWhenATickTakesLongerThanTheLimit() {
        AtomicLong clock = new AtomicLong();
        TickWatchdog watchdog = new TickWatchdog(1000, clock::get, () -> { });

        clock.set(1000);
        assertFalse(watchdog.isStalled());
        clock.set(1001);
        assertTrue(watchdog.isStalled());
        watchdog.tick();
        assertFalse(watchdog.isStalled());
    }
}

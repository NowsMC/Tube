package space.nows.tube.server.connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

class PacketRateLimiterTest {

    @Test
    void allowsUpToTheLimitWithinOneSecond() {
        AtomicLong clock = new AtomicLong();
        PacketRateLimiter limiter = new PacketRateLimiter(3, clock::get);
        UUID player = UUID.randomUUID();

        assertTrue(limiter.allow(player));
        assertTrue(limiter.allow(player));
        assertTrue(limiter.allow(player));
        assertFalse(limiter.allow(player));
    }

    @Test
    void startsANewWindowAfterOneSecond() {
        AtomicLong clock = new AtomicLong();
        PacketRateLimiter limiter = new PacketRateLimiter(1, clock::get);
        UUID player = UUID.randomUUID();

        assertTrue(limiter.allow(player));
        assertFalse(limiter.allow(player));
        clock.addAndGet(1_000_000_000L);
        assertTrue(limiter.allow(player));
    }

    @Test
    void countsEachPlayerSeparately() {
        PacketRateLimiter limiter = new PacketRateLimiter(1, () -> 0);

        assertTrue(limiter.allow(UUID.randomUUID()));
        assertTrue(limiter.allow(UUID.randomUUID()));
    }
}

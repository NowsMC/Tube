package space.nows.tube.server.player;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import space.nows.tube.api.entity.player.Player;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

class IdleKickerTest {

    private final UUID uuid = UUID.randomUUID();
    private final Player player = (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[] {Player.class},
            (proxy, method, args) -> method.getName().equals("uuid") ? uuid : null);

    @Test
    void playerBecomesIdleAfterTheTimeout() {
        AtomicLong clock = new AtomicLong();
        IdleKicker kicker = new IdleKicker(2, clock::get);

        kicker.active(player);
        clock.addAndGet(119_999);
        assertFalse(kicker.isIdle(player));
        clock.addAndGet(1);
        assertTrue(kicker.isIdle(player));
    }

    @Test
    void activityResetsTheTimer() {
        AtomicLong clock = new AtomicLong();
        IdleKicker kicker = new IdleKicker(1, clock::get);

        kicker.active(player);
        clock.addAndGet(59_000);
        kicker.active(player);
        clock.addAndGet(59_000);
        assertFalse(kicker.isIdle(player));
    }

    @Test
    void unknownPlayerIsNotIdle() {
        assertFalse(new IdleKicker(1, () -> Long.MAX_VALUE).isIdle(player));
    }
}

package space.nows.tube.server.connection;

import net.kyori.adventure.text.Component;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerPacketEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public final class PacketRateLimiter {

    private static final long WINDOW_NANOS = 1_000_000_000L;

    private final int packetsPerSecond;
    private final LongSupplier clock;
    private final Map<UUID, Window> windows = new ConcurrentHashMap<>();

    public PacketRateLimiter(int packetsPerSecond, LongSupplier clock) {
        this.packetsPerSecond = packetsPerSecond;
        this.clock = clock;
    }

    public void install(GlobalEventHandler handler) {
        if (packetsPerSecond <= 0) {
            return;
        }
        handler.addListener(PlayerPacketEvent.class, event -> {
            if (!allow(event.getPlayer().getUuid())) {
                event.setCancelled(true);
                event.getPlayer().kick(Component.text("Kicked for exceeding packet rate limit"));
            }
        });
        handler.addListener(PlayerDisconnectEvent.class, event -> windows.remove(event.getPlayer().getUuid()));
    }

    public boolean allow(UUID player) {
        long now = clock.getAsLong();
        Window window = windows.computeIfAbsent(player, ignored -> new Window(now));
        if (now - window.start >= WINDOW_NANOS) {
            window.start = now;
            window.count = 0;
        }
        return ++window.count <= packetsPerSecond;
    }

    private static final class Window {

        private long start;
        private int count;

        private Window(long start) {
            this.start = start;
        }
    }
}

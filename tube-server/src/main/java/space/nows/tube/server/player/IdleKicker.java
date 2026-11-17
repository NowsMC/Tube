package space.nows.tube.server.player;

import space.nows.tube.api.Server;
import space.nows.tube.api.entity.Player;
import space.nows.tube.api.event.BlockBreakEvent;
import space.nows.tube.api.event.BlockPlaceEvent;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.InventoryClickEvent;
import space.nows.tube.api.event.PlayerChatEvent;
import space.nows.tube.api.event.PlayerInteractEvent;
import space.nows.tube.api.event.PlayerJoinEvent;
import space.nows.tube.api.event.PlayerMoveEvent;
import space.nows.tube.api.event.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public final class IdleKicker {

    private static final long CHECK_PERIOD_TICKS = 20;

    private final long timeoutMillis;
    private final LongSupplier clock;
    private final Map<UUID, Long> lastActive = new ConcurrentHashMap<>();

    public IdleKicker(int timeoutMinutes, LongSupplier clock) {
        this.timeoutMillis = timeoutMinutes * 60_000L;
        this.clock = clock;
    }

    public void install(Server server) {
        if (timeoutMillis <= 0) {
            return;
        }
        EventBus events = server.events();
        events.subscribe(PlayerJoinEvent.class, EventPriority.MONITOR, false, event -> active(event.player()));
        events.subscribe(PlayerQuitEvent.class, EventPriority.MONITOR, false, event -> lastActive.remove(event.player().uuid()));
        events.subscribe(PlayerMoveEvent.class, EventPriority.MONITOR, true, event -> {
            if (!event.from().equals(event.to())) {
                active(event.player());
            }
        });
        events.subscribe(PlayerChatEvent.class, EventPriority.MONITOR, false, event -> active(event.player()));
        events.subscribe(PlayerInteractEvent.class, EventPriority.MONITOR, false, event -> active(event.player()));
        events.subscribe(BlockBreakEvent.class, EventPriority.MONITOR, false, event -> active(event.player()));
        events.subscribe(BlockPlaceEvent.class, EventPriority.MONITOR, false, event -> active(event.player()));
        events.subscribe(InventoryClickEvent.class, EventPriority.MONITOR, false, event -> active(event.player()));
        server.scheduler().runRepeating(CHECK_PERIOD_TICKS, CHECK_PERIOD_TICKS, () -> server.players().forEach(this::kickIfIdle));
    }

    public void active(Player player) {
        lastActive.put(player.uuid(), clock.getAsLong());
    }

    public boolean isIdle(Player player) {
        long last = lastActive.getOrDefault(player.uuid(), Long.MIN_VALUE);
        return last != Long.MIN_VALUE && clock.getAsLong() - last >= timeoutMillis;
    }

    private void kickIfIdle(Player player) {
        if (isIdle(player)) {
            lastActive.remove(player.uuid());
            player.kick("You have been idle for too long!");
        }
    }
}

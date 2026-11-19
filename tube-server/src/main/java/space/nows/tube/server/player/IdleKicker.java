package space.nows.tube.server.player;

import space.nows.tube.api.Server;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.block.BlockBreakEvent;
import space.nows.tube.api.event.block.BlockPlaceEvent;
import space.nows.tube.api.event.inventory.InventoryClickEvent;
import space.nows.tube.api.event.player.PlayerChatEvent;
import space.nows.tube.api.event.player.PlayerInteractEvent;
import space.nows.tube.api.event.player.PlayerJoinEvent;
import space.nows.tube.api.event.player.PlayerMoveEvent;
import space.nows.tube.api.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public final class IdleKicker {

    private static final long CHECK_PERIOD_TICKS = 20;

    private volatile long timeoutMillis;
    private final LongSupplier clock;
    private final Map<UUID, Long> lastActive = new ConcurrentHashMap<>();

    public IdleKicker(int timeoutMinutes, LongSupplier clock) {
        this.timeoutMillis = timeoutMinutes * 60_000L;
        this.clock = clock;
    }

    public void setTimeoutMinutes(int minutes) {
        this.timeoutMillis = minutes * 60_000L;
    }

    public void install(Server server) {
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
        long timeout = timeoutMillis;
        return timeout > 0 && last != Long.MIN_VALUE && clock.getAsLong() - last >= timeout;
    }

    private void kickIfIdle(Player player) {
        if (isIdle(player)) {
            lastActive.remove(player.uuid());
            player.kick("You have been idle for too long!");
        }
    }
}

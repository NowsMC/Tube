package space.nows.tube.server.player;

import ca.spottedleaf.concurrentutil.map.concurrent.objects.ConcurrentChainedObject2ObjectHashTable;
import space.nows.tube.api.Player;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.world.WorldService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class PlayerService {

    private final WorldService worlds;
    private final PermissionService permissions;
    private volatile space.nows.tube.api.event.EventBus events;
    private final ConcurrentChainedObject2ObjectHashTable<UUID, MinestomPlayer> players = ConcurrentChainedObject2ObjectHashTable.createWithExpected(64);

    public PlayerService(WorldService worlds, PermissionService permissions) {
        this.worlds = worlds;
        this.permissions = permissions;
        worlds.attach(this);
    }

    public void attachEvents(space.nows.tube.api.event.EventBus events) {
        this.events = events;
    }

    space.nows.tube.api.event.EventBus events() {
        return events;
    }

    public MinestomPlayer wrap(net.minestom.server.entity.Player handle) {
        return players.computeIfAbsent(handle.getUuid(), uuid -> new MinestomPlayer(handle, worlds, permissions, this));
    }

    public void remove(net.minestom.server.entity.Player handle) {
        players.remove(handle.getUuid());
    }

    public Collection<Player> online() {
        return List.copyOf(players.values());
    }
}

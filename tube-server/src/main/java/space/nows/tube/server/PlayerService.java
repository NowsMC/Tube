package space.nows.tube.server;

import ca.spottedleaf.concurrentutil.map.concurrent.objects.ConcurrentChainedObject2ObjectHashTable;
import space.nows.tube.api.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

final class PlayerService {

    private final WorldService worlds;
    private final PermissionService permissions;
    private final ConcurrentChainedObject2ObjectHashTable<UUID, TubePlayer> players = ConcurrentChainedObject2ObjectHashTable.createWithExpected(64);

    PlayerService(WorldService worlds, PermissionService permissions) {
        this.worlds = worlds;
        this.permissions = permissions;
        worlds.attach(this);
    }

    TubePlayer wrap(net.minestom.server.entity.Player handle) {
        return players.computeIfAbsent(handle.getUuid(), uuid -> new TubePlayer(handle, worlds, permissions));
    }

    void remove(net.minestom.server.entity.Player handle) {
        players.remove(handle.getUuid());
    }

    Collection<Player> online() {
        return List.copyOf(players.values());
    }
}

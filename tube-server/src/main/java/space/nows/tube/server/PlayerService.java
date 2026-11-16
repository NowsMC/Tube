package space.nows.tube.server;

import space.nows.tube.api.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class PlayerService {

    private final WorldService worlds;
    private final Map<UUID, TubePlayer> players = new ConcurrentHashMap<>();

    PlayerService(WorldService worlds) {
        this.worlds = worlds;
    }

    TubePlayer wrap(net.minestom.server.entity.Player handle) {
        return players.computeIfAbsent(handle.getUuid(), uuid -> new TubePlayer(handle, worlds));
    }

    void remove(net.minestom.server.entity.Player handle) {
        players.remove(handle.getUuid());
    }

    Collection<Player> online() {
        return List.copyOf(players.values());
    }
}

package space.nows.tube.server.player;

import ca.spottedleaf.concurrentutil.map.concurrent.objects.ConcurrentChainedObject2ObjectHashTable;
import space.nows.tube.api.entity.LoginMode;
import space.nows.tube.api.entity.Player;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.world.WorldService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerService {

    private final WorldService worlds;
    private final PermissionService permissions;
    private volatile space.nows.tube.api.event.EventBus events;
    private final Map<UUID, LoginMode> loginModes = new ConcurrentHashMap<>();
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
        loginModes.remove(handle.getUuid());
    }

    public void rememberLoginMode(UUID uuid, LoginMode mode) {
        loginModes.put(uuid, mode);
    }

    public LoginMode loginMode(UUID uuid) {
        return loginModes.getOrDefault(uuid, LoginMode.OFFLINE);
    }

    public Collection<Player> online() {
        return List.copyOf(players.values());
    }
}

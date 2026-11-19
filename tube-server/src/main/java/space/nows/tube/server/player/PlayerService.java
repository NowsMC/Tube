package space.nows.tube.server.player;

import space.nows.tube.api.entity.player.LoginMode;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.scoreboard.TabLists;
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
    private final Map<UUID, MinestomPlayer> players = new ConcurrentHashMap<>();
    private final TabLists tabLists = new TabLists();
    private final PlayerVisibility visibility = new PlayerVisibility();

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
        return players.computeIfAbsent(handle.getUuid(), uuid -> {
            handle.updateViewableRule(viewer -> visibility.canSee(viewer.getUuid(), uuid));
            return new MinestomPlayer(handle, worlds, permissions, this);
        });
    }

    public TabLists tabLists() {
        return tabLists;
    }

    PlayerVisibility visibility() {
        return visibility;
    }

    public void remove(net.minestom.server.entity.Player handle) {
        visibility.forget(handle.getUuid());
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

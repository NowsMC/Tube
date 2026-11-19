package space.nows.tube.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket.Action;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.scoreboard.tab.TabEntry;
import space.nows.tube.api.scoreboard.tab.TabList;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MinestomTabList implements TabList {

    private static final EnumSet<Action> ADD = EnumSet.of(Action.ADD_PLAYER, Action.UPDATE_LISTED, Action.UPDATE_LATENCY,
            Action.UPDATE_GAME_MODE, Action.UPDATE_DISPLAY_NAME, Action.UPDATE_LIST_ORDER);
    private static final EnumSet<Action> UPDATE = EnumSet.of(Action.UPDATE_LATENCY, Action.UPDATE_GAME_MODE,
            Action.UPDATE_DISPLAY_NAME, Action.UPDATE_LIST_ORDER);

    private final Map<UUID, TabEntry> added = new LinkedHashMap<>();
    private final Map<UUID, TabOverride> overrides = new HashMap<>();
    private volatile net.minestom.server.entity.Player viewer;
    private Component header;
    private Component footer;

    MinestomTabList(net.minestom.server.entity.Player viewer) {
        this.viewer = viewer;
    }

    void attach(net.minestom.server.entity.Player viewer) {
        this.viewer = viewer;
    }

    @Override
    public synchronized void setHeaderAndFooter(Component header, Component footer) {
        this.header = Objects.requireNonNullElse(header, Component.empty());
        this.footer = Objects.requireNonNullElse(footer, Component.empty());
        viewer.sendPlayerListHeaderAndFooter(this.header, this.footer);
    }

    @Override
    public synchronized void setDisplayName(Player target, Component displayName) {
        override(target.uuid(), overrides.getOrDefault(target.uuid(), TabOverride.NONE).withDisplayName(displayName));
        send(displayName(target.uuid(), displayName));
    }

    @Override
    public synchronized void resetDisplayName(Player target) {
        override(target.uuid(), overrides.getOrDefault(target.uuid(), TabOverride.NONE).withDisplayName(null));
        send(displayName(target.uuid(), null));
    }

    @Override
    public synchronized void setOrder(Player target, int order) {
        override(target.uuid(), overrides.getOrDefault(target.uuid(), TabOverride.NONE).withOrder(order));
        send(order(target.uuid(), order));
    }

    @Override
    public synchronized void setLatency(Player target, int latency) {
        override(target.uuid(), overrides.getOrDefault(target.uuid(), TabOverride.NONE).withLatency(latency));
        send(latency(target.uuid(), latency));
    }

    @Override
    public synchronized void setListed(Player target, boolean listed) {
        override(target.uuid(), overrides.getOrDefault(target.uuid(), TabOverride.NONE).withListed(listed));
        send(listed(target.uuid(), listed));
    }

    @Override
    public synchronized void add(TabEntry entry) {
        added.put(entry.id(), entry);
        send(added(entry));
    }

    @Override
    public synchronized void update(TabEntry entry) {
        if (!added.containsKey(entry.id())) {
            throw new IllegalArgumentException("No tab entry " + entry.id() + ", add it first");
        }
        added.put(entry.id(), entry);
        send(new PlayerInfoUpdatePacket(UPDATE, entry(entry, List.of())));
    }

    @Override
    public synchronized void remove(UUID id) {
        added.remove(id);
        send(new PlayerInfoRemovePacket(id));
    }

    @Override
    public synchronized Collection<TabEntry> entries() {
        return List.copyOf(added.values());
    }

    @Override
    public synchronized void reset() {
        if (header != null) {
            header = null;
            footer = null;
            viewer.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
        }
        added.keySet().forEach(id -> send(new PlayerInfoRemovePacket(id)));
        added.clear();
        overrides.forEach((id, override) -> {
            if (override.displayName() != null) {
                send(displayName(id, null));
            }
            if (override.order() != null) {
                send(order(id, 0));
            }
            if (Boolean.FALSE.equals(override.listed())) {
                send(listed(id, true));
            }
        });
        overrides.clear();
    }

    synchronized void replay() {
        if (header != null) {
            viewer.sendPlayerListHeaderAndFooter(header, footer);
        }
        added.values().forEach(entry -> send(added(entry)));
        overrides.forEach((id, override) -> {
            if (MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id) != null) {
                apply(id, override);
            }
        });
    }

    synchronized void targetJoined(UUID id) {
        TabOverride override = overrides.get(id);
        if (override != null && viewer.isOnline()) {
            apply(id, override);
        }
    }

    private void override(UUID id, TabOverride override) {
        overrides.put(id, override);
    }

    private void apply(UUID id, TabOverride override) {
        if (override.displayName() != null) {
            send(displayName(id, override.displayName()));
        }
        if (override.order() != null) {
            send(order(id, override.order()));
        }
        if (override.listed() != null) {
            send(listed(id, override.listed()));
        }
        if (override.latency() != null) {
            send(latency(id, override.latency()));
        }
    }

    private void send(SendablePacket packet) {
        viewer.sendPacket(packet);
    }

    private static PlayerInfoUpdatePacket added(TabEntry entry) {
        List<PlayerInfoUpdatePacket.Property> properties = entry.skin() == null ? List.of()
                : List.of(new PlayerInfoUpdatePacket.Property("textures", entry.skin().textures(), entry.skin().signature()));
        return new PlayerInfoUpdatePacket(ADD, entry(entry, properties));
    }

    private static PlayerInfoUpdatePacket.Entry entry(TabEntry entry, List<PlayerInfoUpdatePacket.Property> properties) {
        return new PlayerInfoUpdatePacket.Entry(entry.id(), entry.name(), properties, true, entry.latency(),
                GameMode.valueOf(entry.gameMode().name()), entry.displayName(), null, entry.order(), true);
    }

    private static PlayerInfoUpdatePacket displayName(UUID id, Component displayName) {
        return change(Action.UPDATE_DISPLAY_NAME, id, displayName, true, 0, 0);
    }

    private static PlayerInfoUpdatePacket order(UUID id, int order) {
        return change(Action.UPDATE_LIST_ORDER, id, null, true, 0, order);
    }

    private static PlayerInfoUpdatePacket latency(UUID id, int latency) {
        return change(Action.UPDATE_LATENCY, id, null, true, latency, 0);
    }

    private static PlayerInfoUpdatePacket listed(UUID id, boolean listed) {
        return change(Action.UPDATE_LISTED, id, null, listed, 0, 0);
    }

    private static PlayerInfoUpdatePacket change(Action action, UUID id, Component displayName, boolean listed, int latency, int order) {
        return new PlayerInfoUpdatePacket(action,
                new PlayerInfoUpdatePacket.Entry(id, "", List.of(), listed, latency, GameMode.SURVIVAL, displayName, null, order, true));
    }
}

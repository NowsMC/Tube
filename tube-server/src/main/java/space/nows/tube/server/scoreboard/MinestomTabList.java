package space.nows.tube.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket.Action;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.scoreboard.tab.TabEntry;
import space.nows.tube.api.scoreboard.tab.TabList;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MinestomTabList implements TabList {

    private static final EnumSet<Action> ADD = EnumSet.of(Action.ADD_PLAYER, Action.UPDATE_LISTED, Action.UPDATE_LATENCY,
            Action.UPDATE_GAME_MODE, Action.UPDATE_DISPLAY_NAME, Action.UPDATE_LIST_ORDER);
    private static final EnumSet<Action> UPDATE = EnumSet.of(Action.UPDATE_LATENCY, Action.UPDATE_GAME_MODE,
            Action.UPDATE_DISPLAY_NAME, Action.UPDATE_LIST_ORDER);

    private final net.minestom.server.entity.Player viewer;
    private final Map<UUID, TabEntry> added = new ConcurrentHashMap<>();

    public MinestomTabList(net.minestom.server.entity.Player viewer) {
        this.viewer = viewer;
    }

    @Override
    public void setHeaderAndFooter(Component header, Component footer) {
        viewer.sendPlayerListHeaderAndFooter(Objects.requireNonNullElse(header, Component.empty()),
                Objects.requireNonNullElse(footer, Component.empty()));
    }

    @Override
    public void setDisplayName(Player target, Component displayName) {
        send(Action.UPDATE_DISPLAY_NAME, target.uuid(), displayName, true, 0, 0);
    }

    @Override
    public void resetDisplayName(Player target) {
        send(Action.UPDATE_DISPLAY_NAME, target.uuid(), null, true, 0, 0);
    }

    @Override
    public void setOrder(Player target, int order) {
        send(Action.UPDATE_LIST_ORDER, target.uuid(), null, true, 0, order);
    }

    @Override
    public void setLatency(Player target, int latency) {
        send(Action.UPDATE_LATENCY, target.uuid(), null, true, latency, 0);
    }

    @Override
    public void setListed(Player target, boolean listed) {
        send(Action.UPDATE_LISTED, target.uuid(), null, listed, 0, 0);
    }

    @Override
    public void add(TabEntry entry) {
        added.put(entry.id(), entry);
        List<PlayerInfoUpdatePacket.Property> properties = entry.skin() == null ? List.of()
                : List.of(new PlayerInfoUpdatePacket.Property("textures", entry.skin().textures(), entry.skin().signature()));
        viewer.sendPacket(new PlayerInfoUpdatePacket(ADD, entry(entry, properties)));
    }

    @Override
    public void update(TabEntry entry) {
        if (!added.containsKey(entry.id())) {
            throw new IllegalArgumentException("No tab entry " + entry.id() + ", add it first");
        }
        added.put(entry.id(), entry);
        viewer.sendPacket(new PlayerInfoUpdatePacket(UPDATE, entry(entry, List.of())));
    }

    @Override
    public void remove(UUID id) {
        added.remove(id);
        viewer.sendPacket(new PlayerInfoRemovePacket(id));
    }

    @Override
    public Collection<TabEntry> entries() {
        return List.copyOf(added.values());
    }

    private static PlayerInfoUpdatePacket.Entry entry(TabEntry entry, List<PlayerInfoUpdatePacket.Property> properties) {
        return new PlayerInfoUpdatePacket.Entry(entry.id(), entry.name(), properties, true, entry.latency(),
                GameMode.valueOf(entry.gameMode().name()), entry.displayName(), null, entry.order(), true);
    }

    private void send(Action action, UUID id, Component displayName, boolean listed, int latency, int order) {
        viewer.sendPacket(new PlayerInfoUpdatePacket(action,
                new PlayerInfoUpdatePacket.Entry(id, "", List.of(), listed, latency, GameMode.SURVIVAL, displayName, null, order, true)));
    }
}

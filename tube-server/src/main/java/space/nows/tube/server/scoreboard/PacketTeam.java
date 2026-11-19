package space.nows.tube.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.TeamColor;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.scoreboard.team.CollisionRule;
import space.nows.tube.api.scoreboard.team.NameTagVisibility;
import space.nows.tube.api.scoreboard.team.Team;
import space.nows.tube.server.player.PlayerService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

final class PacketTeam implements Team {

    private static final byte FRIENDLY_FIRE = 0x01;
    private static final byte SEE_INVISIBLE = 0x02;

    private final String name;
    private final boolean shared;
    private final PlayerService players;
    private final Viewers viewers = new Viewers();
    private final Set<String> members = new LinkedHashSet<>();
    private Component displayName;
    private Component prefix = Component.empty();
    private Component suffix = Component.empty();
    private NamedTextColor color = NamedTextColor.WHITE;
    private NameTagVisibility nameTagVisibility = NameTagVisibility.ALWAYS;
    private CollisionRule collisionRule = CollisionRule.ALWAYS;
    private boolean friendlyFire = true;
    private boolean seeInvisibleMembers = true;

    PacketTeam(String name, boolean shared, PlayerService players) {
        this.name = name;
        this.shared = shared;
        this.players = players;
        this.displayName = Component.text(name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public synchronized Component displayName() {
        return displayName;
    }

    @Override
    public synchronized void setDisplayName(Component displayName) {
        this.displayName = Objects.requireNonNullElse(displayName, Component.empty());
        update();
    }

    @Override
    public synchronized Component prefix() {
        return prefix;
    }

    @Override
    public synchronized void setPrefix(Component prefix) {
        this.prefix = Objects.requireNonNullElse(prefix, Component.empty());
        update();
    }

    @Override
    public synchronized Component suffix() {
        return suffix;
    }

    @Override
    public synchronized void setSuffix(Component suffix) {
        this.suffix = Objects.requireNonNullElse(suffix, Component.empty());
        update();
    }

    @Override
    public synchronized NamedTextColor color() {
        return color;
    }

    @Override
    public synchronized void setColor(NamedTextColor color) {
        this.color = Objects.requireNonNullElse(color, NamedTextColor.WHITE);
        update();
    }

    @Override
    public synchronized NameTagVisibility nameTagVisibility() {
        return nameTagVisibility;
    }

    @Override
    public synchronized void setNameTagVisibility(NameTagVisibility visibility) {
        this.nameTagVisibility = Objects.requireNonNull(visibility, "visibility");
        update();
    }

    @Override
    public synchronized CollisionRule collisionRule() {
        return collisionRule;
    }

    @Override
    public synchronized void setCollisionRule(CollisionRule rule) {
        this.collisionRule = Objects.requireNonNull(rule, "rule");
        update();
    }

    @Override
    public synchronized boolean friendlyFire() {
        return friendlyFire;
    }

    @Override
    public synchronized void setFriendlyFire(boolean allowed) {
        this.friendlyFire = allowed;
        update();
    }

    @Override
    public synchronized boolean seeInvisibleMembers() {
        return seeInvisibleMembers;
    }

    @Override
    public synchronized void setSeeInvisibleMembers(boolean see) {
        this.seeInvisibleMembers = see;
        update();
    }

    @Override
    public boolean shared() {
        return shared;
    }

    @Override
    public synchronized void show(Player player) {
        requirePrivate();
        net.minestom.server.entity.Player handle = Viewers.handle(player);
        if (viewers.add(handle)) {
            handle.sendPacket(created());
        }
    }

    @Override
    public synchronized void hide(Player player) {
        requirePrivate();
        net.minestom.server.entity.Player handle = Viewers.handle(player);
        if (viewers.remove(handle)) {
            handle.sendPacket(packet(new TeamsPacket.RemoveTeamAction()));
        }
    }

    @Override
    public Set<Player> viewers() {
        if (!shared) {
            return viewers.wrapped(players);
        }
        Set<Player> everyone = new LinkedHashSet<>();
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> everyone.add(players.wrap(player)));
        return everyone;
    }

    @Override
    public synchronized Set<String> members() {
        return Set.copyOf(members);
    }

    @Override
    public synchronized void addMember(String entry) {
        if (members.add(entry)) {
            send(packet(new TeamsPacket.AddEntitiesToTeamAction(List.of(entry))));
        }
    }

    @Override
    public synchronized void removeMember(String entry) {
        if (members.remove(entry)) {
            send(packet(new TeamsPacket.RemoveEntitiesToTeamAction(List.of(entry))));
        }
    }

    synchronized void joined(net.minestom.server.entity.Player player) {
        if (shared) {
            player.sendPacket(created());
        }
    }

    void left(UUID id) {
        viewers.forget(id);
    }

    synchronized void removed() {
        send(packet(new TeamsPacket.RemoveTeamAction()));
        viewers.clear();
    }

    private void requirePrivate() {
        if (shared) {
            throw new IllegalStateException("Team " + name + " is shown to every player");
        }
    }

    private void update() {
        send(packet(new TeamsPacket.UpdateTeamAction(settings())));
    }

    private void send(TeamsPacket packet) {
        if (shared) {
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.sendPacket(packet));
        } else {
            viewers.send(packet);
        }
    }

    private TeamsPacket created() {
        return packet(new TeamsPacket.CreateTeamAction(settings(), List.copyOf(members)));
    }

    private TeamsPacket packet(TeamsPacket.Action action) {
        return new TeamsPacket(name, action);
    }

    private TeamsPacket.Settings settings() {
        byte flags = (byte) ((friendlyFire ? FRIENDLY_FIRE : 0) | (seeInvisibleMembers ? SEE_INVISIBLE : 0));
        return new TeamsPacket.Settings(displayName, prefix, suffix,
                TeamsPacket.NameTagVisibility.valueOf(nameTagVisibility.name()),
                TeamsPacket.CollisionRule.valueOf(collisionRule.name()),
                TeamColor.valueOf(NamedTextColor.NAMES.key(color).toUpperCase(Locale.ROOT)), flags);
    }
}

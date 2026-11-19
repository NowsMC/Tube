package space.nows.tube.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.scoreboard.Scoreboards;
import space.nows.tube.api.scoreboard.Sidebar;
import space.nows.tube.api.scoreboard.objective.Objective;
import space.nows.tube.api.scoreboard.objective.ObjectiveSlot;
import space.nows.tube.api.scoreboard.team.Team;
import space.nows.tube.server.player.PlayerService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ScoreboardService implements Scoreboards {

    private final PlayerService players;
    private final Map<String, PacketTeam> teams = new ConcurrentHashMap<>();
    private final Map<String, PacketObjective> objectives = new ConcurrentHashMap<>();

    public ScoreboardService(PlayerService players) {
        this.players = players;
    }

    public void install(GlobalEventHandler handler) {
        handler.addListener(PlayerSpawnEvent.class, event -> {
            if (event.isFirstSpawn()) {
                teams.values().forEach(team -> team.joined(event.getPlayer()));
                players.tabLists().joined(event.getPlayer());
            }
        });
        handler.addListener(PlayerDisconnectEvent.class, event -> {
            teams.values().forEach(team -> team.left(event.getPlayer().getUuid()));
            objectives.values().forEach(objective -> objective.left(event.getPlayer().getUuid()));
        });
    }

    @Override
    public Sidebar createSidebar(Component title) {
        return new MinestomSidebar(title, players);
    }

    @Override
    public Team createTeam(String name) {
        return addTeam(new PacketTeam(name, true, players));
    }

    @Override
    public Team createPrivateTeam(String name) {
        return addTeam(new PacketTeam(name, false, players));
    }

    private synchronized Team addTeam(PacketTeam team) {
        if (teams.putIfAbsent(team.name(), team) != null) {
            throw new IllegalArgumentException("Team " + team.name() + " already exists");
        }
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(team::joined);
        return team;
    }

    @Override
    public Optional<Team> team(String name) {
        return Optional.ofNullable(teams.get(name));
    }

    @Override
    public Collection<Team> teams() {
        return List.copyOf(teams.values());
    }

    @Override
    public synchronized void removeTeam(Team team) {
        if (team instanceof PacketTeam packetTeam && teams.remove(team.name(), packetTeam)) {
            packetTeam.removed();
        }
    }

    @Override
    public synchronized Objective createObjective(String name, ObjectiveSlot slot, Component title) {
        Objects.requireNonNull(slot, "slot");
        PacketObjective objective = new PacketObjective(name, slot, title, this, players);
        if (objectives.putIfAbsent(name, objective) != null) {
            throw new IllegalArgumentException("Objective " + name + " already exists");
        }
        return objective;
    }

    @Override
    public Optional<Objective> objective(String name) {
        return Optional.ofNullable(objectives.get(name));
    }

    @Override
    public Collection<Objective> objectives() {
        return List.copyOf(objectives.values());
    }

    @Override
    public synchronized void removeObjective(Objective objective) {
        if (objective instanceof PacketObjective packetObjective && objectives.remove(objective.name(), packetObjective)) {
            packetObjective.removed();
        }
    }

    void claim(PacketObjective shown, Player player) {
        net.minestom.server.entity.Player handle = Viewers.handle(player);
        for (PacketObjective other : objectives.values()) {
            if (other != shown && other.slot() == shown.slot() && other.shownTo(handle)) {
                other.hide(player);
            }
        }
    }
}

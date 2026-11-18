package space.nows.tube.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import space.nows.tube.api.scoreboard.Scoreboards;
import space.nows.tube.api.scoreboard.Sidebar;
import space.nows.tube.api.scoreboard.Team;
import space.nows.tube.server.player.PlayerService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ScoreboardService implements Scoreboards {

    private final PlayerService players;
    private final Map<String, MinestomTeam> teams = new ConcurrentHashMap<>();

    public ScoreboardService(PlayerService players) {
        this.players = players;
    }

    @Override
    public Sidebar createSidebar(Component title) {
        return new MinestomSidebar(title, players);
    }

    @Override
    public synchronized Team createTeam(String name) {
        if (teams.containsKey(name)) {
            throw new IllegalArgumentException("Team " + name + " already exists");
        }
        MinestomTeam created = new MinestomTeam(MinecraftServer.getTeamManager().createTeam(name));
        teams.put(name, created);
        return created;
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
        if (teams.remove(team.name(), team)) {
            MinecraftServer.getTeamManager().deleteTeam(((MinestomTeam) team).handle());
        }
    }
}

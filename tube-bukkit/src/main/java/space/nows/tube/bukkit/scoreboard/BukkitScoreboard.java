package space.nows.tube.bukkit.scoreboard;

import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import space.nows.tube.api.scoreboard.Sidebar;
import space.nows.tube.bukkit.BukkitRuntime;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BukkitScoreboard implements Scoreboard {

    private final BukkitRuntime runtime;
    private final boolean main;
    private final Map<String, BukkitObjective> objectives = new LinkedHashMap<>();
    private final Map<String, BukkitTeam> teams = new LinkedHashMap<>();
    private final Map<DisplaySlot, BukkitObjective> slots = new java.util.EnumMap<>(DisplaySlot.class);
    private final Set<UUID> viewers = new HashSet<>();
    private Sidebar sidebar;

    BukkitScoreboard(BukkitRuntime runtime, boolean main) {
        this.runtime = runtime;
        this.main = main;
    }

    BukkitRuntime runtime() {
        return runtime;
    }

    boolean isMain() {
        return main;
    }

    @Override
    public synchronized Objective registerNewObjective(String name, String criteria) {
        return registerNewObjective(name, criteria, name);
    }

    @Override
    public synchronized Objective registerNewObjective(String name, String criteria, String displayName) {
        if (objectives.containsKey(name)) {
            throw new IllegalArgumentException("An objective of name '" + name + "' already exists");
        }
        BukkitObjective objective = new BukkitObjective(this, name, criteria, displayName);
        objectives.put(name, objective);
        return objective;
    }

    @Override
    public synchronized Objective getObjective(String name) {
        return objectives.get(name);
    }

    @Override
    public synchronized Objective getObjective(DisplaySlot slot) {
        return slots.get(slot);
    }

    @Override
    public synchronized Set<Objective> getObjectives() {
        return new LinkedHashSet<>(objectives.values());
    }

    @Override
    public synchronized void clearSlot(DisplaySlot slot) {
        BukkitObjective removed = slots.remove(slot);
        if (removed != null) {
            removed.slotCleared();
            refreshSidebar();
        }
    }

    @Override
    public synchronized Set<String> getEntries() {
        Set<String> entries = new LinkedHashSet<>();
        objectives.values().forEach(objective -> entries.addAll(objective.entries()));
        return entries;
    }

    @Override
    public synchronized Set<Score> getScores(String entry) {
        Set<Score> scores = new LinkedHashSet<>();
        objectives.values().stream().filter(objective -> objective.entries().contains(entry)).forEach(objective -> scores.add(objective.getScore(entry)));
        return scores;
    }

    @Override
    public synchronized void resetScores(String entry) {
        objectives.values().forEach(objective -> objective.reset(entry));
        refreshSidebar();
    }

    @Override
    public synchronized Team registerNewTeam(String name) {
        if (teams.containsKey(name)) {
            throw new IllegalArgumentException("Team name '" + name + "' is already in use");
        }
        BukkitTeam team = new BukkitTeam(this, name);
        teams.put(name, team);
        return team;
    }

    @Override
    public synchronized Team getTeam(String name) {
        return teams.get(name);
    }

    @Override
    public synchronized Set<Team> getTeams() {
        return new LinkedHashSet<>(teams.values());
    }

    @Override
    public synchronized Team getEntryTeam(String entry) {
        return teams.values().stream().filter(team -> team.hasEntry(entry)).findFirst().orElse(null);
    }

    @Override
    public Team getPlayerTeam(OfflinePlayer player) {
        return getEntryTeam(player.getName());
    }

    synchronized void forgetObjective(BukkitObjective objective) {
        objectives.remove(objective.getName());
        if (slots.values().remove(objective)) {
            refreshSidebar();
        }
    }

    synchronized void forgetTeam(BukkitTeam team) {
        teams.remove(team.getName());
    }

    synchronized void assignSlot(BukkitObjective objective, DisplaySlot slot) {
        BukkitObjective previous = slots.put(slot, objective);
        if (previous != null && previous != objective) {
            previous.slotCleared();
        }
        refreshSidebar();
    }

    synchronized void refreshSidebar() {
        BukkitObjective shown = slots.get(DisplaySlot.SIDEBAR);
        if (shown == null) {
            if (sidebar != null) {
                sidebar.clear();
                for (var viewer : Set.copyOf(sidebar.viewers())) {
                    sidebar.hide(viewer);
                }
            }
            return;
        }
        if (sidebar == null) {
            sidebar = runtime.tube().scoreboards().createSidebar(shown.title());
        }
        sidebar.setTitle(shown.title());
        Map<String, Integer> scores = shown.scores();
        for (var line : sidebar.lines()) {
            if (!scores.containsKey(line.id())) {
                sidebar.removeLine(line.id());
            }
        }
        scores.forEach((entry, score) -> sidebar.setLine(entry, BukkitText.component(entry), score));
        for (UUID id : viewers) {
            runtime.tube().players().stream().filter(player -> player.uuid().equals(id)).forEach(sidebar::show);
        }
    }

    public synchronized void attach(space.nows.tube.api.entity.player.Player player) {
        viewers.add(player.uuid());
        if (sidebar != null) {
            sidebar.show(player);
        }
    }

    public synchronized void detach(space.nows.tube.api.entity.player.Player player) {
        viewers.remove(player.uuid());
        if (sidebar != null) {
            sidebar.hide(player);
        }
    }
}

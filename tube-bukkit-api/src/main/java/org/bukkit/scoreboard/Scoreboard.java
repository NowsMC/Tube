package org.bukkit.scoreboard;

import java.util.Set;

public interface Scoreboard {

    Objective registerNewObjective(String name, String criteria);

    Objective registerNewObjective(String name, String criteria, String displayName);

    Objective getObjective(String name);

    Objective getObjective(DisplaySlot slot);

    Set<Objective> getObjectives();

    void clearSlot(DisplaySlot slot);

    Set<String> getEntries();

    Set<Score> getScores(String entry);

    void resetScores(String entry);

    Team registerNewTeam(String name);

    Team getTeam(String name);

    Set<Team> getTeams();

    Team getEntryTeam(String entry);

    Team getPlayerTeam(org.bukkit.OfflinePlayer player);
}

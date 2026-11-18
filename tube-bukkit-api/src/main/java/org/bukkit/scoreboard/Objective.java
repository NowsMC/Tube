package org.bukkit.scoreboard;

public interface Objective {

    String getName();

    String getDisplayName();

    void setDisplayName(String displayName);

    String getCriteria();

    DisplaySlot getDisplaySlot();

    void setDisplaySlot(DisplaySlot slot);

    Scoreboard getScoreboard();

    boolean isModifiable();

    Score getScore(String entry);

    Score getScore(org.bukkit.OfflinePlayer player);

    void unregister();
}

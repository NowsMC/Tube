package org.bukkit.scoreboard;

public interface Score {

    String getEntry();

    Objective getObjective();

    Scoreboard getScoreboard();

    int getScore();

    void setScore(int score);

    boolean isScoreSet();
}

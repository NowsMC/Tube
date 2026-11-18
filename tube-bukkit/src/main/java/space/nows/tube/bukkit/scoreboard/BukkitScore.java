package space.nows.tube.bukkit.scoreboard;

import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

final class BukkitScore implements Score {

    private final BukkitObjective objective;
    private final String entry;

    BukkitScore(BukkitObjective objective, String entry) {
        this.objective = objective;
        this.entry = entry;
    }

    @Override
    public String getEntry() {
        return entry;
    }

    @Override
    public Objective getObjective() {
        return objective;
    }

    @Override
    public Scoreboard getScoreboard() {
        return objective.getScoreboard();
    }

    @Override
    public int getScore() {
        Integer score = objective.get(entry);
        return score == null ? 0 : score;
    }

    @Override
    public void setScore(int score) {
        objective.put(entry, score);
        objective.changed();
    }

    @Override
    public boolean isScoreSet() {
        return objective.get(entry) != null;
    }
}

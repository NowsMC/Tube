package space.nows.tube.bukkit.scoreboard;

import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

final class BukkitObjective implements Objective {

    private final BukkitScoreboard scoreboard;
    private final String name;
    private final String criteria;
    private final Map<String, Integer> scores = new LinkedHashMap<>();
    private String displayName;
    private DisplaySlot slot;

    BukkitObjective(BukkitScoreboard scoreboard, String name, String criteria, String displayName) {
        this.scoreboard = scoreboard;
        this.name = name;
        this.criteria = criteria;
        this.displayName = displayName;
    }

    Component title() {
        return BukkitText.component(displayName);
    }

    synchronized Map<String, Integer> scores() {
        return new LinkedHashMap<>(scores);
    }

    synchronized Set<String> entries() {
        return new LinkedHashSet<>(scores.keySet());
    }

    synchronized void reset(String entry) {
        scores.remove(entry);
    }

    synchronized void put(String entry, int score) {
        scores.put(entry, score);
    }

    synchronized Integer get(String entry) {
        return scores.get(entry);
    }

    void slotCleared() {
        this.slot = null;
    }

    void changed() {
        if (slot == DisplaySlot.SIDEBAR) {
            scoreboard.refreshSidebar();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        changed();
    }

    @Override
    public String getCriteria() {
        return criteria;
    }

    @Override
    public DisplaySlot getDisplaySlot() {
        return slot;
    }

    @Override
    public void setDisplaySlot(DisplaySlot slot) {
        if (slot == null) {
            if (this.slot != null) {
                scoreboard.clearSlot(this.slot);
            }
            return;
        }
        this.slot = slot;
        scoreboard.assignSlot(this, slot);
    }

    @Override
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
    public boolean isModifiable() {
        return !criteria.equals("health");
    }

    @Override
    public Score getScore(String entry) {
        return new BukkitScore(this, entry);
    }

    @Override
    public Score getScore(OfflinePlayer player) {
        return getScore(player.getName());
    }

    @Override
    public void unregister() {
        scoreboard.forgetObjective(this);
    }
}

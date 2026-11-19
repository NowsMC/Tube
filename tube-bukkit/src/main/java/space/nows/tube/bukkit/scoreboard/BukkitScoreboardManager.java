package space.nows.tube.bukkit.scoreboard;

import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import space.nows.tube.bukkit.BukkitRuntime;

public final class BukkitScoreboardManager implements ScoreboardManager {

    private final BukkitRuntime runtime;
    private final BukkitScoreboard main;

    public BukkitScoreboardManager(BukkitRuntime runtime) {
        this.runtime = runtime;
        this.main = new BukkitScoreboard(runtime, true);
    }

    @Override
    public Scoreboard getMainScoreboard() {
        return main;
    }

    @Override
    public Scoreboard getNewScoreboard() {
        return new BukkitScoreboard(runtime, false);
    }

    public BukkitScoreboard main() {
        return main;
    }

    public void playerJoined(space.nows.tube.api.entity.player.Player player) {
        main.attach(player);
    }
}

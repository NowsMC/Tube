package space.nows.tube.api.scoreboard;

import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.entity.player.Player;

public final class ScoreHolder {

    private ScoreHolder() {
    }

    public static String of(Entity entity) {
        return entity instanceof Player player ? player.name() : entity.uuid().toString();
    }
}

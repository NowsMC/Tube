package space.nows.tube.api.scoreboard.objective;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.scoreboard.ScoreHolder;

import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

public interface Objective {

    String name();

    ObjectiveSlot slot();

    Component title();

    void setTitle(Component title);

    ScoreStyle style();

    void setStyle(ScoreStyle style);

    void setScore(String entry, int score);

    void resetScore(String entry);

    OptionalInt score(String entry);

    Map<String, Integer> scores();

    void show(Player player);

    void hide(Player player);

    Set<Player> viewers();

    default void setScore(Entity entity, int score) {
        setScore(ScoreHolder.of(entity), score);
    }

    default void resetScore(Entity entity) {
        resetScore(ScoreHolder.of(entity));
    }

    default OptionalInt score(Entity entity) {
        return score(ScoreHolder.of(entity));
    }
}

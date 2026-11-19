package space.nows.tube.api.scoreboard.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.scoreboard.ScoreHolder;

import java.util.Set;

public interface Team {

    String name();

    Component displayName();

    void setDisplayName(Component displayName);

    Component prefix();

    void setPrefix(Component prefix);

    Component suffix();

    void setSuffix(Component suffix);

    NamedTextColor color();

    void setColor(NamedTextColor color);

    NameTagVisibility nameTagVisibility();

    void setNameTagVisibility(NameTagVisibility visibility);

    CollisionRule collisionRule();

    void setCollisionRule(CollisionRule rule);

    boolean friendlyFire();

    void setFriendlyFire(boolean allowed);

    boolean seeInvisibleMembers();

    void setSeeInvisibleMembers(boolean see);

    boolean shared();

    void show(Player player);

    void hide(Player player);

    Set<Player> viewers();

    Set<String> members();

    void addMember(String entry);

    void removeMember(String entry);

    default void addMember(Entity entity) {
        addMember(ScoreHolder.of(entity));
    }

    default void removeMember(Entity entity) {
        removeMember(ScoreHolder.of(entity));
    }

    default boolean hasMember(Entity entity) {
        return members().contains(ScoreHolder.of(entity));
    }

}

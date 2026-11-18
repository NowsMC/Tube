package space.nows.tube.api.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.entity.Player;

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

    Set<String> members();

    void addMember(String entry);

    void removeMember(String entry);

    default void addMember(Entity entity) {
        addMember(entry(entity));
    }

    default void removeMember(Entity entity) {
        removeMember(entry(entity));
    }

    default boolean hasMember(Entity entity) {
        return members().contains(entry(entity));
    }

    private static String entry(Entity entity) {
        return entity instanceof Player player ? player.name() : entity.uuid().toString();
    }
}

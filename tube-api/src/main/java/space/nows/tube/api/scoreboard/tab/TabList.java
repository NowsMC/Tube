package space.nows.tube.api.scoreboard.tab;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.entity.player.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface TabList {

    void setHeaderAndFooter(Component header, Component footer);

    void setDisplayName(Player target, Component displayName);

    void resetDisplayName(Player target);

    void setOrder(Player target, int order);

    void setLatency(Player target, int latency);

    void setListed(Player target, boolean listed);

    void add(TabEntry entry);

    void update(TabEntry entry);

    void remove(UUID id);

    Collection<TabEntry> entries();

    default Optional<TabEntry> entry(UUID id) {
        return entries().stream().filter(entry -> entry.id().equals(id)).findFirst();
    }
}

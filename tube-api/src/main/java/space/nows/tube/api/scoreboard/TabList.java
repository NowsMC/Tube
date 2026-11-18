package space.nows.tube.api.scoreboard;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.entity.Player;

import java.util.Collection;

public interface TabList {

    void setHeaderAndFooter(Component header, Component footer);

    void setDisplayName(Player target, Component displayName);

    void resetDisplayName(Player target);

    void setOrder(Player target, int order);

    void setLatency(Player target, int latency);

    void setListed(Player target, boolean listed);

    void add(TabEntry entry);

    void update(TabEntry entry);

    void remove(java.util.UUID id);

    Collection<TabEntry> entries();
}

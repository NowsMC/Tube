package space.nows.tube.api.scoreboard;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.entity.player.Player;

import java.util.List;
import java.util.Set;

public interface Sidebar {

    Component title();

    void setTitle(Component title);

    void setLine(String id, Component text, int score);

    void removeLine(String id);

    void clear();

    List<SidebarLine> lines();

    boolean numbersHidden();

    void setNumbersHidden(boolean hidden);

    void show(Player player);

    void hide(Player player);

    Set<Player> viewers();
}

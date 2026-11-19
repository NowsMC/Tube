package space.nows.tube.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.scoreboard.Sidebar.NumberFormat;
import net.minestom.server.scoreboard.Sidebar.ScoreboardLine;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.scoreboard.Sidebar;
import space.nows.tube.api.scoreboard.SidebarLine;
import space.nows.tube.server.player.MinestomPlayer;
import space.nows.tube.server.player.PlayerService;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class MinestomSidebar implements Sidebar {

    private final net.minestom.server.scoreboard.Sidebar sidebar;
    private final PlayerService players;
    private boolean numbersHidden;

    MinestomSidebar(Component title, PlayerService players) {
        this.sidebar = new net.minestom.server.scoreboard.Sidebar(Objects.requireNonNullElse(title, Component.empty()));
        this.players = players;
    }

    @Override
    public Component title() {
        return sidebar.getTitle();
    }

    @Override
    public void setTitle(Component title) {
        sidebar.setTitle(Objects.requireNonNullElse(title, Component.empty()));
    }

    @Override
    public void setLine(String id, Component text, int score) {
        Component content = Objects.requireNonNullElse(text, Component.empty());
        if (sidebar.getLine(id) == null) {
            sidebar.createLine(new ScoreboardLine(id, content, score, format()));
            return;
        }
        sidebar.updateLineContent(id, content);
        sidebar.updateLineScore(id, score);
    }

    @Override
    public void removeLine(String id) {
        if (sidebar.getLine(id) != null) {
            sidebar.removeLine(id);
        }
    }

    @Override
    public void clear() {
        for (ScoreboardLine line : Set.copyOf(sidebar.getLines())) {
            sidebar.removeLine(line.getId());
        }
    }

    @Override
    public List<SidebarLine> lines() {
        return sidebar.getLines().stream()
                .sorted(Comparator.comparingInt(ScoreboardLine::getLine).reversed())
                .map(line -> new SidebarLine(line.getId(), line.getContent(), line.getLine()))
                .toList();
    }

    @Override
    public boolean numbersHidden() {
        return numbersHidden;
    }

    @Override
    public void setNumbersHidden(boolean hidden) {
        this.numbersHidden = hidden;
        for (ScoreboardLine line : sidebar.getLines()) {
            sidebar.updateLineNumberFormat(line.getId(), format());
        }
    }

    private NumberFormat format() {
        return numbersHidden ? NumberFormat.blank() : null;
    }

    @Override
    public void show(Player player) {
        sidebar.addViewer(((MinestomPlayer) player).handle());
    }

    @Override
    public void hide(Player player) {
        sidebar.removeViewer(((MinestomPlayer) player).handle());
    }

    @Override
    public Set<Player> viewers() {
        Set<Player> viewers = new LinkedHashSet<>();
        for (net.minestom.server.entity.Player viewer : sidebar.getViewers()) {
            if (viewer.isOnline()) {
                viewers.add(players.wrap(viewer));
            }
        }
        return viewers;
    }
}

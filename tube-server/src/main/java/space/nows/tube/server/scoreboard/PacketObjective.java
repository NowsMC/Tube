package space.nows.tube.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ResetScorePacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.scoreboard.objective.Objective;
import space.nows.tube.api.scoreboard.objective.ObjectiveSlot;
import space.nows.tube.api.scoreboard.objective.ScoreStyle;
import space.nows.tube.server.player.PlayerService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

final class PacketObjective implements Objective {

    private static final byte CREATE = 0;
    private static final byte REMOVE = 1;
    private static final byte UPDATE = 2;

    private final String name;
    private final ObjectiveSlot slot;
    private final ScoreboardService service;
    private final PlayerService players;
    private final Viewers viewers = new Viewers();
    private final Map<String, Integer> scores = new LinkedHashMap<>();
    private Component title;
    private ScoreStyle style = ScoreStyle.NUMBER;

    PacketObjective(String name, ObjectiveSlot slot, Component title, ScoreboardService service, PlayerService players) {
        this.name = name;
        this.slot = slot;
        this.title = Objects.requireNonNullElse(title, Component.empty());
        this.service = service;
        this.players = players;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ObjectiveSlot slot() {
        return slot;
    }

    @Override
    public synchronized Component title() {
        return title;
    }

    @Override
    public synchronized void setTitle(Component title) {
        this.title = Objects.requireNonNullElse(title, Component.empty());
        viewers.send(objective(UPDATE));
    }

    @Override
    public synchronized ScoreStyle style() {
        return style;
    }

    @Override
    public synchronized void setStyle(ScoreStyle style) {
        this.style = Objects.requireNonNull(style, "style");
        viewers.send(objective(UPDATE));
    }

    @Override
    public synchronized void setScore(String entry, int score) {
        Integer previous = scores.put(entry, score);
        if (previous == null || previous != score) {
            viewers.send(score(entry, score));
        }
    }

    @Override
    public synchronized void resetScore(String entry) {
        if (scores.remove(entry) != null) {
            viewers.send(new ResetScorePacket(entry, name));
        }
    }

    @Override
    public synchronized OptionalInt score(String entry) {
        Integer score = scores.get(entry);
        return score == null ? OptionalInt.empty() : OptionalInt.of(score);
    }

    @Override
    public synchronized Map<String, Integer> scores() {
        return Map.copyOf(scores);
    }

    @Override
    public void show(Player player) {
        net.minestom.server.entity.Player handle = Viewers.handle(player);
        service.claim(this, player);
        synchronized (this) {
            if (!viewers.add(handle)) {
                return;
            }
            handle.sendPacket(objective(CREATE));
            scores.forEach((entry, score) -> handle.sendPacket(score(entry, score)));
            handle.sendPacket(new DisplayScoreboardPacket(position(), name));
        }
    }

    @Override
    public synchronized void hide(Player player) {
        net.minestom.server.entity.Player handle = Viewers.handle(player);
        if (viewers.remove(handle)) {
            handle.sendPacket(objective(REMOVE));
        }
    }

    @Override
    public Set<Player> viewers() {
        return viewers.wrapped(players);
    }

    synchronized boolean shownTo(net.minestom.server.entity.Player player) {
        return viewers.contains(player);
    }

    void left(UUID id) {
        viewers.forget(id);
    }

    synchronized void removed() {
        viewers.send(objective(REMOVE));
        viewers.clear();
    }

    private byte position() {
        return (byte) (slot == ObjectiveSlot.TAB_LIST ? 0 : 2);
    }

    private ScoreboardObjectivePacket objective(byte mode) {
        ScoreboardObjectivePacket.Type type = style == ScoreStyle.HEARTS ? ScoreboardObjectivePacket.Type.HEARTS : ScoreboardObjectivePacket.Type.INTEGER;
        return new ScoreboardObjectivePacket(name, mode, title, type, null);
    }

    private UpdateScorePacket score(String entry, int score) {
        return new UpdateScorePacket(entry, name, score, null, null);
    }
}

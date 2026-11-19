package space.nows.tube.api.scoreboard.tab;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.entity.player.GameMode;
import space.nows.tube.api.entity.player.Skin;

import java.util.Objects;
import java.util.UUID;

public record TabEntry(UUID id, String name, Component displayName, Skin skin, int latency, GameMode gameMode, int order) {

    public TabEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(gameMode, "gameMode");
    }

    public static TabEntry of(UUID id, String name) {
        return new TabEntry(id, name, null, null, 0, GameMode.SURVIVAL, 0);
    }

    public TabEntry withDisplayName(Component displayName) {
        return new TabEntry(id, name, displayName, skin, latency, gameMode, order);
    }

    public TabEntry withSkin(Skin skin) {
        return new TabEntry(id, name, displayName, skin, latency, gameMode, order);
    }

    public TabEntry withLatency(int latency) {
        return new TabEntry(id, name, displayName, skin, latency, gameMode, order);
    }

    public TabEntry withGameMode(GameMode gameMode) {
        return new TabEntry(id, name, displayName, skin, latency, gameMode, order);
    }

    public TabEntry withOrder(int order) {
        return new TabEntry(id, name, displayName, skin, latency, gameMode, order);
    }
}

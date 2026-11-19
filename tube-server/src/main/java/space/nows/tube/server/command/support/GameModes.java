package space.nows.tube.server.command.support;

import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.entity.player.GameMode;

import java.util.List;
import java.util.Locale;

public final class GameModes {

    public static final List<String> NAMES = List.of("survival", "creative", "adventure", "spectator");

    private GameModes() {
    }

    public static GameMode parse(String text) {
        return switch (text.toLowerCase(Locale.ROOT)) {
            case "survival", "s", "0" -> GameMode.SURVIVAL;
            case "creative", "c", "1" -> GameMode.CREATIVE;
            case "adventure", "a", "2" -> GameMode.ADVENTURE;
            case "spectator", "sp", "3" -> GameMode.SPECTATOR;
            default -> throw new CommandFailure("Unknown game mode " + text);
        };
    }

    public static String name(GameMode mode) {
        return mode.name().toLowerCase(Locale.ROOT);
    }
}

package space.nows.tube.api;

import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Player extends CommandSender {

    UUID uuid();

    World world();

    Location location();

    CompletableFuture<Void> teleport(Location location);

    GameMode gameMode();

    void setGameMode(GameMode gameMode);

    double health();

    void setHealth(double health);

    void sendActionBar(Component message);

    void showTitle(Component title, Component subtitle);

    void kick(Component reason);

    default void kick(String reason) {
        kick(Component.text(reason));
    }
}

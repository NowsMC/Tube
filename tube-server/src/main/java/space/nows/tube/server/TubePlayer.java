package space.nows.tube.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import space.nows.tube.api.GameMode;
import space.nows.tube.api.Location;
import space.nows.tube.api.Player;
import space.nows.tube.api.World;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class TubePlayer implements Player {

    private final net.minestom.server.entity.Player handle;
    private final WorldService worlds;

    TubePlayer(net.minestom.server.entity.Player handle, WorldService worlds) {
        this.handle = handle;
        this.worlds = worlds;
    }

    @Override
    public String name() {
        return handle.getUsername();
    }

    @Override
    public UUID uuid() {
        return handle.getUuid();
    }

    @Override
    public World world() {
        return worlds.of(handle.getInstance());
    }

    @Override
    public Location location() {
        Pos pos = handle.getPosition();
        return new Location(world(), pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
    }

    @Override
    public CompletableFuture<Void> teleport(Location location) {
        Pos pos = new Pos(location.x(), location.y(), location.z(), location.yaw(), location.pitch());
        Instance target = worlds.instanceOf(location.world());
        if (target == handle.getInstance()) {
            return handle.teleport(pos);
        }
        return handle.setInstance(target, pos);
    }

    @Override
    public GameMode gameMode() {
        return GameMode.valueOf(handle.getGameMode().name());
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        handle.setGameMode(net.minestom.server.entity.GameMode.valueOf(gameMode.name()));
    }

    @Override
    public double health() {
        return handle.getHealth();
    }

    @Override
    public void setHealth(double health) {
        handle.setHealth((float) health);
    }

    @Override
    public void sendActionBar(Component message) {
        handle.sendActionBar(message);
    }

    @Override
    public void showTitle(Component title, Component subtitle) {
        handle.showTitle(Title.title(title, subtitle));
    }

    @Override
    public void kick(Component reason) {
        handle.kick(reason);
    }

    @Override
    public void sendMessage(Component message) {
        handle.sendMessage(message);
    }
}

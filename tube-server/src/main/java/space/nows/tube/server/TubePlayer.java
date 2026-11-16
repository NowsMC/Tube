package space.nows.tube.server;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import space.nows.tube.api.Location;
import space.nows.tube.api.Player;
import space.nows.tube.api.World;

import java.util.UUID;

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
    public void teleport(Location location) {
        Pos pos = new Pos(location.x(), location.y(), location.z(), location.yaw(), location.pitch());
        Instance target = worlds.instanceOf(location.world());
        if (target == handle.getInstance()) {
            handle.teleport(pos);
        } else {
            handle.setInstance(target, pos);
        }
    }

    @Override
    public void kick(String reason) {
        handle.kick(Component.text(reason));
    }

    @Override
    public void sendMessage(String message) {
        handle.sendMessage(message);
    }
}

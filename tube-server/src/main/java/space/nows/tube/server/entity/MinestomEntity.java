package space.nows.tube.server.entity;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.world.Location;
import space.nows.tube.api.world.World;
import space.nows.tube.server.world.WorldService;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MinestomEntity implements Entity {

    protected final net.minestom.server.entity.Entity handle;
    protected final WorldService worlds;

    public MinestomEntity(net.minestom.server.entity.Entity handle, WorldService worlds) {
        this.handle = handle;
        this.worlds = worlds;
    }

    public net.minestom.server.entity.Entity minestom() {
        return handle;
    }

    @Override
    public UUID uuid() {
        return handle.getUuid();
    }

    @Override
    public String type() {
        return handle.getEntityType().key().asString();
    }

    @Override
    public World world() {
        Instance instance = handle.getInstance();
        if (instance == null) {
            throw new IllegalStateException("Entity is not in a world");
        }
        return worlds.of(instance);
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
    public Optional<Component> customName() {
        return Optional.ofNullable(handle.getCustomName());
    }

    @Override
    public void setCustomName(Component name) {
        handle.setCustomName(name);
    }

    @Override
    public boolean isRemoved() {
        return handle.isRemoved();
    }

    @Override
    public void remove() {
        handle.remove();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MinestomEntity entity && entity.handle == handle;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(handle);
    }
}

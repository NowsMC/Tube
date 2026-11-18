package space.nows.tube.server.entity;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.world.Location;
import space.nows.tube.api.world.World;
import space.nows.tube.server.world.WorldService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MinestomEntity implements Entity {

    private static final Tag<List<String>> TAGS = Tag.String("tube:tags").list().defaultValue(List.of());

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
    public double eyeHeight() {
        return handle.getEyeHeight();
    }

    @Override
    public boolean isSneaking() {
        return handle.getEntityMeta().isSneaking();
    }

    @Override
    public boolean isSprinting() {
        return handle.getEntityMeta().isSprinting();
    }

    @Override
    public boolean isSwimming() {
        return handle.getEntityMeta().isSwimming();
    }

    @Override
    public boolean isGlowing() {
        return handle.isGlowing();
    }

    @Override
    public void setGlowing(boolean glowing) {
        handle.setGlowing(glowing);
    }

    @Override
    public void remove() {
        handle.remove();
    }

    @Override
    public Set<String> tags() {
        return Set.copyOf(handle.getTag(TAGS));
    }

    @Override
    public boolean addTag(String tag) {
        List<String> current = handle.getTag(TAGS);
        if (current.contains(tag)) {
            return false;
        }
        List<String> updated = new ArrayList<>(current);
        updated.add(tag);
        handle.setTag(TAGS, updated);
        return true;
    }

    @Override
    public boolean removeTag(String tag) {
        List<String> updated = new ArrayList<>(handle.getTag(TAGS));
        if (!updated.remove(tag)) {
            return false;
        }
        handle.setTag(TAGS, updated);
        return true;
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

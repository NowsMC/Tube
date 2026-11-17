package space.nows.tube.api.event;

import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.world.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ExplosionEvent implements Cancellable {

    public record BlockPosition(int x, int y, int z) {
    }

    private final Location location;
    private final Entity source;
    private final List<BlockPosition> blocks;
    private float power;
    private boolean cancelled;

    public ExplosionEvent(Location location, Entity source, float power, List<BlockPosition> blocks) {
        this.location = location;
        this.source = source;
        this.power = power;
        this.blocks = new ArrayList<>(blocks);
    }

    public Location location() {
        return location;
    }

    public Optional<Entity> source() {
        return Optional.ofNullable(source);
    }

    public float power() {
        return power;
    }

    public void setPower(float power) {
        this.power = Math.max(0, power);
    }

    public List<BlockPosition> blocks() {
        return blocks;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

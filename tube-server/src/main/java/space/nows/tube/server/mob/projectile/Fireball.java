package space.nows.tube.server.mob.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.move.Liquids;

public final class Fireball extends Entity {

    private static final double ACCELERATION = 0.1;
    private static final double INERTIA = 0.95;
    private static final double WATER_INERTIA = 0.8;
    private static final double TICKS_PER_SECOND = 20;
    private static final int LIFETIME = 1200;

    private final Entity owner;
    private final Impact impact;
    private Vec motion;

    public Fireball(EntityType type, Entity owner, Vec direction, Impact impact) {
        super(type);
        this.owner = owner;
        this.impact = impact;
        this.motion = direction.normalize().mul(ACCELERATION);
        setNoGravity(true);
    }

    public Entity owner() {
        return owner;
    }

    @Override
    public void update(long time) {
        Instance instance = getInstance();
        if (instance == null || isRemoved()) {
            return;
        }
        if (getAliveTicks() > LIFETIME || owner != null && owner.isRemoved() || !instance.isChunkLoaded(getPosition())) {
            remove();
            return;
        }
        double inertia = Liquids.isWater(instance.getBlock(getPosition())) ? WATER_INERTIA : INERTIA;
        motion = motion.add(motion.normalize().mul(ACCELERATION)).mul(inertia);
        Trajectory path = Trajectory.sweep(this, owner, motion);
        if (path.entity() != null) {
            impact.hitEntity(this, path.entity());
            remove();
            return;
        }
        if (path.block() != null) {
            impact.hitBlock(this, path.block(), path.before());
            remove();
            return;
        }
        Pos from = getPosition();
        refreshPosition(from.add(motion).withView(from.yaw(), from.pitch()));
        setVelocity(motion.mul(TICKS_PER_SECOND));
    }
}

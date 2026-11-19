package space.nows.tube.server.mob.projectile;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.move.Liquids;

public final class Fireball extends Entity {

    private static final double ACCELERATION = 0.1;
    private static final double INERTIA = 0.95;
    private static final double WATER_INERTIA = 0.8;
    private static final double STEP = 0.25;
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
        Pos from = getPosition();
        double length = motion.length();
        int steps = Math.max(1, (int) Math.ceil(length / STEP));
        Point before = from;
        for (int i = 1; i <= steps; i++) {
            Point at = from.add(motion.mul((double) i / steps));
            LivingEntity struck = struck(instance, at);
            if (struck != null) {
                impact.hitEntity(this, struck);
                remove();
                return;
            }
            if (!instance.isChunkLoaded(at) || instance.getBlock(at).solid()) {
                impact.hitBlock(this, at, before);
                remove();
                return;
            }
            before = at;
        }
        refreshPosition(from.add(motion).withView(from.yaw(), from.pitch()));
        setVelocity(motion.mul(TICKS_PER_SECOND));
    }

    private LivingEntity struck(Instance instance, Point at) {
        for (Entity entity : instance.getNearbyEntities(at, 2)) {
            if (entity == this || !(entity instanceof LivingEntity living) || living.isDead()
                    || entity == owner) {
                continue;
            }
            if (inside(entity, at)) {
                return living;
            }
        }
        return null;
    }

    private boolean inside(Entity entity, Point at) {
        double reach = getBoundingBox().width() / 2;
        double half = entity.getBoundingBox().width() / 2 + reach;
        Pos p = entity.getPosition();
        return Math.abs(at.x() - p.x()) <= half && Math.abs(at.z() - p.z()) <= half
                && at.y() >= p.y() - reach && at.y() <= p.y() + entity.getBoundingBox().height() + reach;
    }
}

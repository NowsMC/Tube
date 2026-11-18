package space.nows.tube.server.gameplay;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;

public final class SweptArrow extends EntityProjectile {

    private static final double TICKS_PER_SECOND = 20;
    private static final double HIT_MARGIN = 0.3;
    private static final long SHOOTER_GRACE_TICKS = 5;

    public SweptArrow(Entity shooter) {
        super(shooter, EntityType.ARROW);
    }

    @Override
    public void tick(long time) {
        if (!isRemoved() && getInstance() != null && !isOnGround()) {
            sweep();
        }
        if (!isRemoved()) {
            super.tick(time);
        }
    }

    private void sweep() {
        Instance instance = getInstance();
        Pos from = getPosition();
        Vec step = getVelocity().div(TICKS_PER_SECOND);
        double length = step.length();
        if (length < 1.0E-7) {
            return;
        }
        LivingEntity hit = null;
        double nearest = Double.MAX_VALUE;
        for (Entity entity : instance.getNearbyEntities(from, length + 2)) {
            if (!(entity instanceof LivingEntity living) || !hittable(living)) {
                continue;
            }
            double distance = entry(from, step, living);
            if (distance >= 0 && distance < nearest) {
                nearest = distance;
                hit = living;
            }
        }
        if (hit != null) {
            Pos at = from.add(step.mul(nearest));
            EventDispatcher.call(new ProjectileCollideWithEntityEvent(this, at, hit));
        }
    }

    private boolean hittable(LivingEntity entity) {
        if (entity.isDead() || entity.isRemoved()) {
            return false;
        }
        if (entity == getShooter() && getAliveTicks() < SHOOTER_GRACE_TICKS) {
            return false;
        }
        return !(entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR);
    }

    private static double entry(Pos from, Vec step, LivingEntity entity) {
        BoundingBox box = entity.getBoundingBox();
        Pos at = entity.getPosition();
        double[] min = {at.x() + box.minX() - HIT_MARGIN, at.y() + box.minY() - HIT_MARGIN, at.z() + box.minZ() - HIT_MARGIN};
        double[] max = {at.x() + box.maxX() + HIT_MARGIN, at.y() + box.maxY() + HIT_MARGIN, at.z() + box.maxZ() + HIT_MARGIN};
        double[] origin = {from.x(), from.y(), from.z()};
        double[] delta = {step.x(), step.y(), step.z()};
        double enter = 0;
        double exit = 1;
        for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(delta[axis]) < 1.0E-9) {
                if (origin[axis] < min[axis] || origin[axis] > max[axis]) {
                    return -1;
                }
                continue;
            }
            double t1 = (min[axis] - origin[axis]) / delta[axis];
            double t2 = (max[axis] - origin[axis]) / delta[axis];
            enter = Math.max(enter, Math.min(t1, t2));
            exit = Math.min(exit, Math.max(t1, t2));
            if (enter > exit) {
                return -1;
            }
        }
        return enter;
    }
}

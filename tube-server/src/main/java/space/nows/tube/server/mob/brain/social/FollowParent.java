package space.nows.tube.server.mob.brain.social;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.life.Lifecycle;

public final class FollowParent extends Behavior {

    private static final double HORIZONTAL_SCAN = 8.0;
    private static final double VERTICAL_SCAN = 4.0;
    private static final double CLOSE_ENOUGH_SQUARED = 9.0;
    private static final double TOO_FAR_SQUARED = 256.0;
    private static final int REPATH_TICKS = 10;

    private final Creature creature;
    private final Lifecycle life;
    private final double speed;
    private Creature parent;
    private int repathIn;

    public FollowParent(Creature creature, Lifecycle life, double speed) {
        this.creature = creature;
        this.life = life;
        this.speed = speed;
    }

    @Override
    public boolean canStart() {
        if (!life.baby()) {
            return false;
        }
        Creature closest = null;
        double closestDistance = Double.MAX_VALUE;
        Pos origin = creature.getPosition();
        for (Entity entity : creature.getInstance().getNearbyEntities(origin, HORIZONTAL_SCAN * 1.75)) {
            if (!(entity instanceof Creature other) || other.kind() != creature.kind() || !adult(other)) {
                continue;
            }
            Pos at = other.getPosition();
            if (Math.abs(at.x() - origin.x()) > HORIZONTAL_SCAN || Math.abs(at.z() - origin.z()) > HORIZONTAL_SCAN || Math.abs(at.y() - origin.y()) > VERTICAL_SCAN) {
                continue;
            }
            double distance = origin.distanceSquared(at);
            if (distance <= closestDistance) {
                closestDistance = distance;
                closest = other;
            }
        }
        if (closest == null || closestDistance < CLOSE_ENOUGH_SQUARED) {
            return false;
        }
        parent = closest;
        return true;
    }

    @Override
    public boolean canContinue() {
        if (!life.baby() || parent.isDead()) {
            return false;
        }
        double distance = creature.getPosition().distanceSquared(parent.getPosition());
        return distance >= CLOSE_ENOUGH_SQUARED && distance <= TOO_FAR_SQUARED;
    }

    @Override
    public void start() {
        repathIn = 0;
    }

    @Override
    public void stop() {
        parent = null;
    }

    @Override
    public void tick() {
        if (--repathIn <= 0) {
            repathIn = ticks(REPATH_TICKS);
            creature.navigation().moveTo(parent, speed);
        }
    }

    private static boolean adult(Creature other) {
        return other.trait(Lifecycle.class).map(life -> !life.baby()).orElse(!other.isBaby());
    }
}

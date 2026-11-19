package space.nows.tube.server.mob.brain.combat;

import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;

public final class RangedAttack extends Behavior {

    private static final int SIGHT_BEFORE_STANDING = 5;

    private final Creature creature;
    private final double speed;
    private final int minInterval;
    private final int maxInterval;
    private final float radius;
    private final Volley volley;
    private LivingEntity target;
    private int attackIn = -1;
    private int seen;

    public RangedAttack(Creature creature, double speed, int minInterval, int maxInterval, float radius, Volley volley) {
        super(Control.MOVE, Control.LOOK);
        this.creature = creature;
        this.speed = speed;
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
        this.radius = radius;
        this.volley = volley;
    }

    public RangedAttack(Creature creature, double speed, int interval, float radius, Volley volley) {
        this(creature, speed, interval, interval, radius, volley);
    }

    @Override
    public boolean canStart() {
        LivingEntity candidate = creature.target();
        if (candidate == null) {
            return false;
        }
        target = candidate;
        return true;
    }

    @Override
    public boolean canContinue() {
        return canStart() || target != null && !target.isDead() && !creature.navigation().isDone();
    }

    @Override
    public void stop() {
        target = null;
        seen = 0;
        attackIn = -1;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        double distance = creature.getPosition().distanceSquared(target.getPosition());
        boolean visible = creature.sight().canSee(target);
        seen = visible ? seen + 1 : 0;
        if (distance <= radius * radius && seen >= SIGHT_BEFORE_STANDING) {
            creature.navigation().stop();
        } else {
            creature.navigation().moveTo(target, speed);
        }
        creature.gaze().lookAt(target, 30f, 30f);
        double reach = Math.sqrt(distance) / radius;
        if (--attackIn == 0) {
            if (!visible) {
                return;
            }
            volley.fire(creature, target, (float) Math.clamp(reach, 0.1, 1.0));
            attackIn = (int) Math.floor(reach * (maxInterval - minInterval) + minInterval);
        } else if (attackIn < 0) {
            attackIn = (int) Math.floor(minInterval + reach * (maxInterval - minInterval));
        }
    }
}

package space.nows.tube.server.mob.brain.target;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;

abstract class TargetBehavior extends Behavior {

    private static final int DEFAULT_UNSEEN_MEMORY = 60;

    protected final Creature creature;
    private final boolean mustSee;
    private int unseenTicks;
    protected int unseenMemory = DEFAULT_UNSEEN_MEMORY;
    protected LivingEntity remembered;

    protected TargetBehavior(Creature creature, boolean mustSee) {
        super(Control.TARGET);
        this.creature = creature;
        this.mustSee = mustSee;
    }

    protected double followRange() {
        return creature.getAttributeValue(Attribute.FOLLOW_RANGE);
    }

    @Override
    public boolean canContinue() {
        LivingEntity target = creature.target();
        if (target == null) {
            target = remembered;
        }
        if (target == null || !Targeting.combat(0).blind().test(creature, target)) {
            return false;
        }
        double range = followRange();
        if (creature.getPosition().distanceSquared(target.getPosition()) > range * range) {
            return false;
        }
        if (mustSee) {
            if (creature.sight().canSee(target)) {
                unseenTicks = 0;
            } else if (++unseenTicks > halved(unseenMemory)) {
                return false;
            }
        }
        creature.setTarget(target);
        return true;
    }

    @Override
    public void start() {
        unseenTicks = 0;
    }

    @Override
    public void stop() {
        creature.setTarget(null);
        remembered = null;
    }
}

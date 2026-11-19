package space.nows.tube.server.mob.brain.combat;

import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;

public final class Fuse extends Behavior {

    private static final double IGNITE_DISTANCE_SQUARED = 9.0;
    private static final double DEFUSE_DISTANCE_SQUARED = 49.0;

    private final Creature creature;
    private final Swelling swelling;
    private LivingEntity target;

    public Fuse(Creature creature, Swelling swelling) {
        super(Control.MOVE);
        this.creature = creature;
        this.swelling = swelling;
    }

    @Override
    public boolean canStart() {
        LivingEntity current = creature.target();
        return swelling.direction() > 0 || current != null && creature.getPosition().distanceSquared(current.getPosition()) < IGNITE_DISTANCE_SQUARED;
    }

    @Override
    public void start() {
        creature.navigation().stop();
        target = creature.target();
    }

    @Override
    public void stop() {
        target = null;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        boolean keep = target != null && !target.isDead()
                && creature.getPosition().distanceSquared(target.getPosition()) <= DEFUSE_DISTANCE_SQUARED
                && creature.sight().canSee(target);
        swelling.setDirection(keep ? 1 : -1);
    }
}

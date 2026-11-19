package space.nows.tube.server.mob.brain.combat;

import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;

public final class Stalk extends Behavior {

    private static final double GIVE_UP = 225;
    private static final double SPRINT_WITHIN = 16;
    private static final double WALK = 0.8;
    private static final double SPRINT = 1.33;
    private static final double CREEP = 0.6;
    private static final int ATTACK_INTERVAL = 20;

    private final Creature creature;
    private LivingEntity prey;
    private int attackIn;

    public Stalk(Creature creature) {
        super(Control.MOVE, Control.LOOK);
        this.creature = creature;
    }

    @Override
    public boolean canStart() {
        prey = creature.target();
        return prey != null;
    }

    @Override
    public boolean canContinue() {
        if (prey == null || prey.isDead()) {
            return false;
        }
        return creature.getPosition().distanceSquared(prey.getPosition()) <= GIVE_UP && (!creature.navigation().isDone() || canStart());
    }

    @Override
    public void stop() {
        prey = null;
        creature.navigation().stop();
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        creature.gaze().lookAt(prey, 30f, 30f);
        double reach = creature.getBoundingBox().width() * 2;
        double distance = creature.getPosition().distanceSquared(prey.getPosition());
        double speed = WALK;
        if (distance > reach * reach && distance < SPRINT_WITHIN) {
            speed = SPRINT;
        } else if (distance < GIVE_UP) {
            speed = CREEP;
        }
        creature.navigation().moveTo(prey, speed);
        attackIn = Math.max(attackIn - 1, 0);
        if (distance <= reach * reach && attackIn <= 0) {
            attackIn = ATTACK_INTERVAL;
            creature.strike(prey);
        }
    }
}

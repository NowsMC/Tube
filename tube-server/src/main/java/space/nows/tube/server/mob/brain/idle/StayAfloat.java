package space.nows.tube.server.mob.brain.idle;

import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;

public final class StayAfloat extends Behavior {

    private static final float JUMP_CHANCE = 0.8f;

    private final Creature creature;

    public StayAfloat(Creature creature) {
        super(Control.JUMP);
        this.creature = creature;
    }

    @Override
    public boolean canStart() {
        return creature.inWater() || creature.inLava();
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        if (creature.random().nextFloat() < JUMP_CHANCE) {
            creature.jump();
        }
    }
}

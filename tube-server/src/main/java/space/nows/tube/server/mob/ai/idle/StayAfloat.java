package space.nows.tube.server.mob.ai.idle;

import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.ai.Control;
import space.nows.tube.server.mob.ai.Goal;

public final class StayAfloat extends Goal {

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

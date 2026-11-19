package space.nows.tube.server.mob.brain.social;

import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.life.Tameness;

public final class StaySeated extends Behavior {

    private final Creature pet;
    private final Tameness tameness;

    public StaySeated(Creature pet, Tameness tameness) {
        super(Control.MOVE, Control.JUMP);
        this.pet = pet;
        this.tameness = tameness;
    }

    @Override
    public boolean canStart() {
        return tameness.tamed() && tameness.sitting() && !pet.inWater() && pet.isOnGround();
    }

    @Override
    public boolean canContinue() {
        return tameness.sitting();
    }

    @Override
    public void start() {
        pet.navigation().stop();
    }
}

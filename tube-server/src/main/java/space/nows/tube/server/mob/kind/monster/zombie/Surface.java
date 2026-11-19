package space.nows.tube.server.mob.kind.monster.zombie;

import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.Swim;
import space.nows.tube.server.mob.sense.Daylight;

final class Surface extends Behavior {

    private static final int SEA_LEVEL = 63;
    private static final double SWIM_SPEED = 0.06;

    private final Creature drowned;

    Surface(Creature drowned) {
        super(Control.MOVE);
        this.drowned = drowned;
    }

    @Override
    public boolean canStart() {
        return !Daylight.brightOutside(drowned.getInstance()) && drowned.inWater() && drowned.getPosition().y() < SEA_LEVEL - 2;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        Swim.steer(drowned, drowned.getPosition().withY(SEA_LEVEL - 1), SWIM_SPEED);
    }
}

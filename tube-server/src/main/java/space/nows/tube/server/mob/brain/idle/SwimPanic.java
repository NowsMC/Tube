package space.nows.tube.server.mob.brain.idle;

import net.minestom.server.coordinate.Point;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.Swim;

public final class SwimPanic extends Behavior {

    private static final int RANGE = 5;
    private static final int DEPTH = 4;
    private static final int MAX_TICKS = 100;
    private static final double ARRIVED = 0.7;

    private final Creature creature;
    private final double speed;
    private Point wanted;
    private int swum;

    public SwimPanic(Creature creature, double blocksPerTick) {
        super(Control.MOVE);
        this.creature = creature;
        this.speed = blocksPerTick;
    }

    @Override
    public boolean canStart() {
        if (!creature.inWater() || !creature.harm().panicking(creature.getAliveTicks())) {
            return false;
        }
        wanted = Swim.spot(creature, RANGE, DEPTH);
        return wanted != null;
    }

    @Override
    public boolean canContinue() {
        return wanted != null && creature.inWater() && swum < MAX_TICKS && creature.getPosition().distance(wanted) > ARRIVED;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void start() {
        swum = 0;
    }

    @Override
    public void stop() {
        wanted = null;
    }

    @Override
    public void tick() {
        swum++;
        Swim.steer(creature, wanted, speed);
    }
}

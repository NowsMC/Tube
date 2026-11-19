package space.nows.tube.server.mob.brain.idle;

import net.minestom.server.coordinate.Point;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.Swim;

public final class SwimAbout extends Behavior {

    private static final int INTERVAL = 40;
    private static final int RANGE = 6;
    private static final int DEPTH_RANGE = 3;
    private static final int MAX_TICKS = 200;
    private static final double ARRIVED = 0.7;

    private final Creature creature;
    private final double speed;
    private Point wanted;
    private int swum;

    public SwimAbout(Creature creature, double blocksPerTick) {
        super(Control.MOVE);
        this.creature = creature;
        this.speed = blocksPerTick;
    }

    @Override
    public boolean canStart() {
        if (!creature.inWater() || creature.random().nextInt(halved(INTERVAL)) != 0) {
            return false;
        }
        wanted = pick();
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
        creature.setVelocity(creature.getVelocity().mul(0.2));
    }

    @Override
    public void tick() {
        swum++;
        Swim.steer(creature, wanted, speed);
    }

    private Point pick() {
        return Swim.spot(creature, RANGE, DEPTH_RANGE);
    }
}

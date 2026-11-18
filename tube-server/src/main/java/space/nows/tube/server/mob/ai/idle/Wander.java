package space.nows.tube.server.mob.ai.idle;

import net.minestom.server.coordinate.Point;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.ai.Control;
import space.nows.tube.server.mob.ai.Goal;
import space.nows.tube.server.mob.move.RandomSpot;

public final class Wander extends Goal {

    private static final int DEFAULT_INTERVAL = 120;
    private static final float WATER_WANDER_CHANCE = 0.001f;

    private final Creature creature;
    private final double speed;
    private final int interval;
    private final boolean avoidWater;
    private Point wanted;

    private Wander(Creature creature, double speed, int interval, boolean avoidWater) {
        super(Control.MOVE);
        this.creature = creature;
        this.speed = speed;
        this.interval = interval;
        this.avoidWater = avoidWater;
    }

    public static Wander anywhere(Creature creature, double speed) {
        return new Wander(creature, speed, DEFAULT_INTERVAL, false);
    }

    public static Wander onLand(Creature creature, double speed) {
        return new Wander(creature, speed, DEFAULT_INTERVAL, true);
    }

    @Override
    public boolean canStart() {
        if (creature.random().nextInt(halved(interval)) != 0) {
            return false;
        }
        wanted = pick();
        return wanted != null;
    }

    private Point pick() {
        if (!avoidWater) {
            return RandomSpot.anywhere(creature, 10, 7);
        }
        if (creature.inWater()) {
            Point land = RandomSpot.onLand(creature, 15, 7);
            return land == null ? RandomSpot.anywhere(creature, 10, 7) : land;
        }
        return creature.random().nextFloat() >= WATER_WANDER_CHANCE ? RandomSpot.onLand(creature, 10, 7) : RandomSpot.anywhere(creature, 10, 7);
    }

    @Override
    public boolean canContinue() {
        return !creature.navigation().isDone();
    }

    @Override
    public void start() {
        creature.navigation().moveTo(wanted, speed);
    }

    @Override
    public void stop() {
        creature.navigation().stop();
    }
}

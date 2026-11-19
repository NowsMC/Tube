package space.nows.tube.server.mob.brain.idle;

import net.minestom.server.coordinate.Point;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.RandomSpot;

public final class Wander extends Behavior {

    private static final int DEFAULT_INTERVAL = 120;
    private static final float WATER_WANDER_CHANCE = 0.001f;
    private static final int RESTLESS_LIMIT = 100;

    private final Creature creature;
    private final double speed;
    private final int interval;
    private final boolean avoidWater;
    private final float anywhereChance;
    private Point wanted;

    private Wander(Creature creature, double speed, int interval, boolean avoidWater, float anywhereChance) {
        super(Control.MOVE);
        this.creature = creature;
        this.speed = speed;
        this.interval = interval;
        this.avoidWater = avoidWater;
        this.anywhereChance = anywhereChance;
    }

    public static Wander anywhere(Creature creature, double speed) {
        return new Wander(creature, speed, DEFAULT_INTERVAL, false, 1f);
    }

    public static Wander anywhere(Creature creature, double speed, int interval) {
        return new Wander(creature, speed, interval, false, 1f);
    }

    public static Wander onLand(Creature creature, double speed) {
        return new Wander(creature, speed, DEFAULT_INTERVAL, true, WATER_WANDER_CHANCE);
    }

    public static Wander onLandOnly(Creature creature, double speed) {
        return new Wander(creature, speed, DEFAULT_INTERVAL, true, 0f);
    }

    @Override
    public boolean canStart() {
        if (creature.idleTicks() >= RESTLESS_LIMIT || creature.random().nextInt(halved(interval)) != 0) {
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
        return creature.random().nextFloat() >= anywhereChance ? RandomSpot.onLand(creature, 10, 7) : RandomSpot.anywhere(creature, 10, 7);
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

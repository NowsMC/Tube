package space.nows.tube.server.mob.brain.idle;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.target.Targeting;

import java.util.function.Predicate;

public final class WatchNearby extends Behavior {

    private static final float DEFAULT_CHANCE = 0.02f;
    private static final double SEARCH_HEIGHT = 3.0;

    private final Creature creature;
    private final float distance;
    private final float chance;
    private final Predicate<Entity> kind;
    private final boolean players;
    private LivingEntity watched;
    private int remaining;

    private WatchNearby(Creature creature, float distance, float chance, Predicate<Entity> kind, boolean players) {
        super(Control.LOOK);
        this.creature = creature;
        this.distance = distance;
        this.chance = chance;
        this.kind = kind;
        this.players = players;
    }

    public static WatchNearby players(Creature creature, float distance) {
        return new WatchNearby(creature, distance, DEFAULT_CHANCE, entity -> entity instanceof Player, true);
    }

    public static WatchNearby of(Creature creature, Predicate<Entity> kind, float distance) {
        return new WatchNearby(creature, distance, DEFAULT_CHANCE, kind, false);
    }

    @Override
    public boolean canStart() {
        if (creature.random().nextFloat() >= chance) {
            return false;
        }
        Targeting targeting = Targeting.peaceful(distance);
        watched = players ? targeting.nearestPlayer(creature) : targeting.nearest(creature, distance, SEARCH_HEIGHT, kind);
        return watched != null;
    }

    @Override
    public boolean canContinue() {
        if (watched.isDead() || watched.isRemoved()) {
            return false;
        }
        return creature.getPosition().distanceSquared(watched.getPosition()) <= distance * distance && remaining > 0;
    }

    @Override
    public void start() {
        remaining = ticks(40 + creature.random().nextInt(40));
    }

    @Override
    public void stop() {
        watched = null;
    }

    @Override
    public void tick() {
        if (!watched.isDead()) {
            creature.gaze().lookAt(watched);
            remaining--;
        }
    }
}

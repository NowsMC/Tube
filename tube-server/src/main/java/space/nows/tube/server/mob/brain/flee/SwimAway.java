package space.nows.tube.server.mob.brain.flee;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.Swim;

import java.util.function.Predicate;

public final class SwimAway extends Behavior {

    private static final int RANGE = 16;
    private static final int DEPTH = 7;
    private static final double SPRINT_WITHIN = 49;
    private static final double ARRIVED = 0.7;

    private final Creature creature;
    private final Predicate<Entity> kind;
    private final float distance;
    private final double walkSpeed;
    private final double sprintSpeed;
    private Entity threat;
    private Point refuge;

    public SwimAway(Creature creature, Predicate<Entity> kind, float distance, double walkSpeed, double sprintSpeed) {
        super(Control.MOVE);
        this.creature = creature;
        this.kind = kind;
        this.distance = distance;
        this.walkSpeed = walkSpeed;
        this.sprintSpeed = sprintSpeed;
    }

    public static SwimAway players(Creature creature, float distance, double walkSpeed, double sprintSpeed) {
        return new SwimAway(creature, entity -> entity instanceof Player player && player.getGameMode() != GameMode.SPECTATOR,
                distance, walkSpeed, sprintSpeed);
    }

    @Override
    public boolean canStart() {
        if (!creature.inWater()) {
            return false;
        }
        threat = null;
        double best = Double.MAX_VALUE;
        for (Entity entity : creature.getInstance().getNearbyEntities(creature.getPosition(), distance)) {
            double gap = entity.getPosition().distanceSquared(creature.getPosition());
            if (entity != creature && kind.test(entity) && gap < best) {
                best = gap;
                threat = entity;
            }
        }
        if (threat == null) {
            return false;
        }
        refuge = Swim.away(creature, threat.getPosition(), RANGE, DEPTH);
        return refuge != null;
    }

    @Override
    public boolean canContinue() {
        return refuge != null && creature.inWater() && creature.getPosition().distance(refuge) > ARRIVED;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void stop() {
        threat = null;
        refuge = null;
    }

    @Override
    public void tick() {
        boolean close = threat != null && threat.getPosition().distanceSquared(creature.getPosition()) < SPRINT_WITHIN;
        Swim.steer(creature, refuge, close ? sprintSpeed : walkSpeed);
    }
}

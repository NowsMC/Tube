package space.nows.tube.server.mob.ai.social;

import net.minestom.server.entity.Entity;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.ai.Control;
import space.nows.tube.server.mob.ai.Goal;
import space.nows.tube.server.mob.life.Birth;
import space.nows.tube.server.mob.life.Lifecycle;

public final class Breed extends Goal {

    private static final double PARTNER_RANGE = 8.0;
    private static final int COURTSHIP_TICKS = 60;
    private static final double MATING_DISTANCE_SQUARED = 9.0;

    private final Creature creature;
    private final Lifecycle life;
    private final double speed;
    private Creature partner;
    private int courtship;

    public Breed(Creature creature, Lifecycle life, double speed) {
        super(Control.MOVE, Control.LOOK);
        this.creature = creature;
        this.life = life;
        this.speed = speed;
    }

    @Override
    public boolean canStart() {
        if (!life.inLove()) {
            return false;
        }
        partner = freePartner();
        return partner != null;
    }

    @Override
    public boolean canContinue() {
        return !partner.isDead() && loving(partner) && courtship < COURTSHIP_TICKS && !partner.harm().panicking(partner.getAliveTicks());
    }

    @Override
    public void stop() {
        partner = null;
        courtship = 0;
    }

    @Override
    public void tick() {
        creature.gaze().lookAt(partner, 10f, creature.kind().maxHeadPitch());
        creature.navigation().moveTo(partner, speed);
        courtship++;
        if (courtship >= ticks(COURTSHIP_TICKS) && creature.getPosition().distanceSquared(partner.getPosition()) < MATING_DISTANCE_SQUARED) {
            Birth.deliver(creature, partner);
        }
    }

    private Creature freePartner() {
        Creature best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Entity entity : creature.getInstance().getNearbyEntities(creature.getPosition(), PARTNER_RANGE * 1.75)) {
            if (!(entity instanceof Creature other) || other == creature || other.kind() != creature.kind() || !loving(other)
                    || other.harm().panicking(other.getAliveTicks())) {
                continue;
            }
            double distance = creature.getPosition().distanceSquared(other.getPosition());
            if (distance < bestDistance && withinBox(other)) {
                bestDistance = distance;
                best = other;
            }
        }
        return best;
    }

    private boolean withinBox(Creature other) {
        var a = creature.getPosition();
        var b = other.getPosition();
        return Math.abs(a.x() - b.x()) <= PARTNER_RANGE + 1 && Math.abs(a.y() - b.y()) <= PARTNER_RANGE + 1 && Math.abs(a.z() - b.z()) <= PARTNER_RANGE + 1;
    }

    private static boolean loving(Creature other) {
        return other.trait(Lifecycle.class).map(Lifecycle::inLove).orElse(false);
    }
}

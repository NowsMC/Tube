package space.nows.tube.server.mob.brain.target;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.server.mob.Creature;

import java.util.Set;

public final class Retaliate extends TargetBehavior {

    private static final int REVENGE_MEMORY = 300;
    private static final double ALERT_HEIGHT = 10;

    private final Set<EntityType> ignoredAttackers;
    private boolean alertOthers;
    private Set<EntityType> notAlerted = Set.of();
    private int handledAttack = -1;

    public Retaliate(Creature creature, EntityType... ignoredAttackers) {
        super(creature, true);
        this.ignoredAttackers = Set.of(ignoredAttackers);
    }

    public Retaliate alertingOthers(EntityType... except) {
        this.alertOthers = true;
        this.notAlerted = Set.of(except);
        return this;
    }

    @Override
    public boolean canStart() {
        LivingEntity attacker = creature.harm().attacker(creature.getAliveTicks());
        if (attacker == null || creature.harm().attackCount() == handledAttack || ignoredAttackers.contains(attacker.getEntityType())) {
            return false;
        }
        return Targeting.combat(0).blind().ignoringInvisibility().test(creature, attacker);
    }

    @Override
    public void start() {
        LivingEntity attacker = creature.harm().attacker(creature.getAliveTicks());
        creature.setTarget(attacker);
        remembered = attacker;
        handledAttack = creature.harm().attackCount();
        unseenMemory = REVENGE_MEMORY;
        if (alertOthers && attacker != null) {
            alert(attacker);
        }
        super.start();
    }

    private void alert(LivingEntity attacker) {
        double range = followRange();
        Point origin = creature.getPosition();
        for (Entity entity : creature.getInstance().getNearbyEntities(origin, range * 1.75)) {
            if (!(entity instanceof Creature other) || other == creature || other.kind() != creature.kind()
                    || other.target() != null || notAlerted.contains(other.getEntityType())) {
                continue;
            }
            Point at = other.getPosition();
            if (Math.abs(at.x() - origin.x()) <= range + 1 && Math.abs(at.z() - origin.z()) <= range + 1 && Math.abs(at.y() - origin.y()) <= ALERT_HEIGHT + 1) {
                other.setTarget(attacker);
            }
        }
    }
}

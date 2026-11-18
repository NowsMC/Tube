package space.nows.tube.server.mob.ai.combat;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.ai.Control;
import space.nows.tube.server.mob.ai.Goal;

public final class Pounce extends Goal {

    private static final double TICKS_PER_SECOND = 20;

    private final Creature creature;
    private final float upward;
    private LivingEntity target;

    public Pounce(Creature creature, float upward) {
        super(Control.JUMP, Control.MOVE);
        this.creature = creature;
        this.upward = upward;
    }

    @Override
    public boolean canStart() {
        target = creature.target();
        if (target == null) {
            return false;
        }
        double distance = creature.getPosition().distanceSquared(target.getPosition());
        return distance >= 4 && distance <= 16 && creature.isOnGround() && creature.random().nextInt(halved(5)) == 0;
    }

    @Override
    public boolean canContinue() {
        return !creature.isOnGround();
    }

    @Override
    public void start() {
        Pos from = creature.getPosition();
        Pos to = target.getPosition();
        Vec current = creature.getVelocity().div(TICKS_PER_SECOND);
        Vec leap = new Vec(to.x() - from.x(), 0, to.z() - from.z());
        if (leap.lengthSquared() > 1.0E-7) {
            leap = leap.normalize().mul(0.4).add(current.mul(0.2));
        }
        creature.setVelocity(new Vec(leap.x(), upward, leap.z()).mul(TICKS_PER_SECOND));
    }
}

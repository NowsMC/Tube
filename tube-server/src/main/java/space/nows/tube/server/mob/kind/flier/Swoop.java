package space.nows.tube.server.mob.kind.flier;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.combat.Reach;
import space.nows.tube.server.mob.trait.Trait;

final class Swoop implements Trait {

    private static final double TICKS_PER_SECOND = 20;
    private static final double CRUISE = 0.35;
    private static final double DIVE = 0.6;
    private static final double TURN = 0.1;
    private static final int MIN_WAIT = 160;
    private static final int MAX_WAIT = 320;
    private static final double CIRCLE_HEIGHT = 20;

    private final Creature phantom;
    private Point anchor;
    private double angle;
    private double radius;
    private boolean diving;
    private int nextDive;

    Swoop(Creature phantom) {
        this.phantom = phantom;
        phantom.setNoGravity(true);
        this.radius = 5 + phantom.random().nextDouble() * 10;
        this.nextDive = MIN_WAIT;
    }

    @Override
    public void tick() {
        if (phantom.isDead()) {
            return;
        }
        LivingEntity target = phantom.target();
        if (anchor == null || target != null && !diving) {
            Point base = target != null ? target.getPosition() : phantom.getPosition();
            anchor = base.add(0, target != null ? CIRCLE_HEIGHT : 0, 0);
        }
        Point aim;
        double speed;
        if (diving && target != null) {
            aim = target.getPosition().add(0, target.getBoundingBox().height() / 2, 0);
            speed = DIVE;
            if (Reach.melee(phantom, target)) {
                phantom.strike(target);
                diving = false;
            }
        } else {
            diving = false;
            angle += 0.05 + 0.02 * phantom.random().nextDouble();
            aim = anchor.add(Math.cos(angle) * radius, Math.sin(angle * 2) * 2, Math.sin(angle) * radius);
            speed = CRUISE;
            if (target != null && --nextDive <= 0) {
                diving = true;
                nextDive = MIN_WAIT + phantom.random().nextInt(MAX_WAIT - MIN_WAIT);
            }
        }
        Vec heading = aim.sub(phantom.getPosition()).asVec();
        if (heading.lengthSquared() < 1.0E-6) {
            return;
        }
        Vec wanted = heading.normalize().mul(speed);
        Vec current = phantom.getVelocity().div(TICKS_PER_SECOND);
        Vec next = current.add(wanted.sub(current).mul(TURN));
        phantom.setVelocity(next.mul(TICKS_PER_SECOND));
        phantom.setView((float) Math.toDegrees(-Math.atan2(next.x(), next.z())),
                (float) -Math.toDegrees(Math.atan2(next.y(), Math.hypot(next.x(), next.z()))));
    }
}

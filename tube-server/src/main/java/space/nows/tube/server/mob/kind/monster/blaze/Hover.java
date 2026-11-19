package space.nows.tube.server.mob.kind.monster.blaze;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

final class Hover implements Trait {

    private static final double FALL_DAMPING = 0.6;
    private static final double TICKS_PER_SECOND = 20;
    private static final double RISE_TARGET = 0.3;
    private static final double RISE_RATE = 0.3;
    private static final int RETHINK = 100;
    private static final double OFFSET_MODE = 0.5;
    private static final double OFFSET_SPREAD = 6.891;

    private final Creature blaze;
    private double heightOffset = OFFSET_MODE;
    private int rethinkIn;

    Hover(Creature blaze) {
        this.blaze = blaze;
    }

    @Override
    public void tick() {
        Vec velocity = blaze.getVelocity();
        if (!blaze.isOnGround() && velocity.y() < 0) {
            velocity = velocity.withY(velocity.y() * FALL_DAMPING);
        }
        if (--rethinkIn <= 0) {
            rethinkIn = RETHINK;
            heightOffset = OFFSET_MODE + OFFSET_SPREAD * (blaze.random().nextDouble() - blaze.random().nextDouble());
        }
        LivingEntity target = blaze.target();
        if (target != null && target.getPosition().y() + target.getEyeHeight() > blaze.getPosition().y() + blaze.getEyeHeight() + heightOffset) {
            double rise = velocity.y() / TICKS_PER_SECOND;
            velocity = velocity.withY((rise + (RISE_TARGET - rise) * RISE_RATE) * TICKS_PER_SECOND);
        }
        blaze.setVelocity(velocity);
    }
}

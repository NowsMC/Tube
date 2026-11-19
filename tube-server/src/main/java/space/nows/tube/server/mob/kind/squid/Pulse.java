package space.nows.tube.server.mob.kind.squid;

import net.minestom.server.coordinate.Vec;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

final class Pulse implements Trait {

    private static final double FULL_TURN = Math.PI * 2;
    private static final double TICKS_PER_SECOND = 20;
    private static final double STROKE_END = 0.75;
    private static final double GLIDE = 0.9;

    private final Creature squid;
    private Vec stroke = Vec.ZERO;
    private double phase;
    private double rate;

    Pulse(Creature squid) {
        this.squid = squid;
        this.rate = 1.0 / (squid.random().nextFloat() + 1.0) * 0.2;
    }

    Vec stroke() {
        return stroke;
    }

    void stroke(Vec blocksPerTick) {
        this.stroke = blocksPerTick;
    }

    @Override
    public void tick() {
        phase += rate;
        if (phase > FULL_TURN) {
            phase -= FULL_TURN;
            if (squid.random().nextInt(10) == 0) {
                rate = 1.0 / (squid.random().nextFloat() + 1.0) * 0.2;
            }
        }
        if (!squid.inWater()) {
            return;
        }
        if (phase < Math.PI) {
            if (phase / Math.PI > STROKE_END) {
                squid.setVelocity(stroke.mul(TICKS_PER_SECOND));
            }
        } else {
            squid.setVelocity(squid.getVelocity().mul(GLIDE));
        }
        Vec motion = squid.getVelocity();
        if (motion.lengthSquared() > 1.0E-6) {
            squid.setView((float) Math.toDegrees(-Math.atan2(motion.x(), motion.z())), squid.getPosition().pitch());
        }
    }
}

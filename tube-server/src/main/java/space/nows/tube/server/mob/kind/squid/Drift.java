package space.nows.tube.server.mob.kind.squid;

import net.minestom.server.coordinate.Vec;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.server.mob.Creature;

final class Drift extends Behavior {

    private static final int RESTLESS_LIMIT = 100;
    private static final int CHANGE_ONE_IN = 50;
    private static final double SPEED = 0.2;

    private final Creature squid;
    private final Pulse pulse;

    Drift(Creature squid, Pulse pulse) {
        this.squid = squid;
        this.pulse = pulse;
    }

    @Override
    public boolean canStart() {
        return true;
    }

    @Override
    public void tick() {
        if (squid.idleTicks() > RESTLESS_LIMIT) {
            pulse.stroke(Vec.ZERO);
        } else if (squid.random().nextInt(halved(CHANGE_ONE_IN)) == 0 || !squid.inWater() || pulse.stroke().isZero()) {
            double angle = squid.random().nextDouble() * Math.PI * 2;
            pulse.stroke(new Vec(Math.cos(angle) * SPEED, -0.1 + squid.random().nextDouble() * SPEED, Math.sin(angle) * SPEED));
        }
    }
}

package space.nows.tube.server.mob.kind.squid;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.Liquids;

final class Ink extends Behavior {

    private static final double FLEE_SPEED = 3.0;
    private static final double SLOWDOWN_FROM = 5.0;
    private static final double MAX_RANGE = 100;
    private static final double TICKS_PER_SECOND = 20;

    private final Creature squid;
    private final Pulse pulse;

    Ink(Creature squid, Pulse pulse) {
        this.squid = squid;
        this.pulse = pulse;
    }

    private LivingEntity attacker() {
        return squid.harm().attacker(squid.getAliveTicks());
    }

    @Override
    public boolean canStart() {
        LivingEntity attacker = attacker();
        return squid.inWater() && attacker != null && squid.getPosition().distanceSquared(attacker.getPosition()) < MAX_RANGE;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity attacker = attacker();
        if (attacker == null) {
            return;
        }
        Vec away = squid.getPosition().sub(attacker.getPosition()).asVec();
        Block ahead = squid.blockAt(squid.getPosition().add(away));
        if (!Liquids.isWater(ahead) && !ahead.air()) {
            return;
        }
        double length = away.length();
        if (length > 0) {
            away = away.normalize();
            double speed = FLEE_SPEED;
            if (length > SLOWDOWN_FROM) {
                speed -= (length - SLOWDOWN_FROM) / SLOWDOWN_FROM;
            }
            if (speed > 0) {
                away = away.mul(speed);
            }
        }
        if (ahead.air()) {
            away = away.withY(0);
        }
        pulse.stroke(away.div(TICKS_PER_SECOND));
    }
}

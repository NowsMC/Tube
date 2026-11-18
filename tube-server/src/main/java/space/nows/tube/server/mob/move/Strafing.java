package space.nows.tube.server.mob.move;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.attribute.Attribute;

public final class Strafing {

    private static final double TICKS_PER_SECOND = 20;

    private Strafing() {
    }

    public static void step(EntityCreature creature, float forward, float sideways) {
        double speed = creature.getAttributeValue(Attribute.MOVEMENT_SPEED);
        double inputForward = forward * speed;
        double inputSideways = sideways * speed;
        double length = Math.sqrt(inputForward * inputForward + inputSideways * inputSideways);
        if (length < 1.0E-7) {
            return;
        }
        double perTick = Stride.perTick(Math.min(length, 1.0), false);
        double yaw = Math.toRadians(creature.getPosition().yaw());
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        double x = (inputSideways * cos - inputForward * sin) / length;
        double z = (inputForward * cos + inputSideways * sin) / length;
        Vec velocity = creature.getVelocity();
        creature.setVelocity(new Vec(x * perTick * TICKS_PER_SECOND, velocity.y(), z * perTick * TICKS_PER_SECOND));
    }
}

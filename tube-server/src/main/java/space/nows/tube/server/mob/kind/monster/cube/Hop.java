package space.nows.tube.server.mob.kind.monster.cube;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.look.Angles;
import space.nows.tube.server.mob.sound.MobVoice;
import space.nows.tube.server.mob.trait.Trait;

final class Hop implements Trait {

    private static final float TURN_LIMIT = 90f;
    private static final double TICKS_PER_SECOND = 20;
    private static final double GROUND_PUSH = 0.21600002 / Math.pow(0.6 * 0.91, 3);
    private static final double AIR_PUSH = 0.02;

    private final Creature cube;
    private final Cube kind;
    private float heading;
    private boolean aggressive;
    private double wanted;
    private int jumpDelay;

    Hop(Creature cube, Cube kind) {
        this.cube = cube;
        this.kind = kind;
        this.heading = cube.getPosition().yaw();
    }

    void face(float yaw, boolean aggressive) {
        this.heading = yaw;
        this.aggressive = aggressive;
    }

    void want(double speedModifier) {
        this.wanted = speedModifier;
    }

    @Override
    public void tick() {
        float yaw = cube.getPosition().yaw();
        float turned = yaw + Math.clamp(Angles.wrap(heading - yaw), -TURN_LIMIT, TURN_LIMIT);
        cube.setView(turned, cube.getPosition().pitch());
        if (wanted <= 0) {
            return;
        }
        double speed = wanted * cube.getAttributeValue(Attribute.MOVEMENT_SPEED);
        wanted = 0;
        double radians = Math.toRadians(turned);
        Vec forward = new Vec(-Math.sin(radians), 0, Math.cos(radians));
        Vec velocity = cube.getVelocity();
        if (!cube.isOnGround()) {
            cube.setVelocity(velocity.add(forward.mul(speed * AIR_PUSH * TICKS_PER_SECOND)));
            return;
        }
        if (jumpDelay-- > 0) {
            return;
        }
        jumpDelay = kind.jumpDelay(cube);
        if (aggressive) {
            jumpDelay /= 3;
        }
        Vec push = forward.mul(speed * GROUND_PUSH * TICKS_PER_SECOND);
        cube.setVelocity(new Vec(velocity.x() + push.x(), kind.jumpPower(cube) * TICKS_PER_SECOND, velocity.z() + push.z()));
        MobVoice.play(cube, kind.jumpSound(cube));
    }
}

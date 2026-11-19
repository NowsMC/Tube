package space.nows.tube.server.mob.kind.monster.vex;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

final class Glide implements Trait {

    private static final double TICKS_PER_SECOND = 20;
    private static final double PULL = 0.05;
    private static final double BRAKE = 0.5;

    private final Creature vex;
    private Point wanted;
    private double speed;

    Glide(Creature vex) {
        this.vex = vex;
        vex.setNoGravity(true);
    }

    boolean busy() {
        return wanted != null;
    }

    void moveTo(Point point, double speed) {
        this.wanted = point;
        this.speed = speed;
    }

    @Override
    public void tick() {
        if (wanted == null) {
            return;
        }
        Vec delta = Vec.fromPoint(wanted.sub(vex.getPosition()));
        double length = delta.length();
        double size = (vex.getBoundingBox().width() * 2 + vex.getBoundingBox().height()) / 3;
        if (length < size) {
            wanted = null;
            vex.setVelocity(vex.getVelocity().mul(BRAKE));
            return;
        }
        vex.setVelocity(vex.getVelocity().add(delta.mul(speed * PULL / length * TICKS_PER_SECOND)));
        LivingEntity target = vex.target();
        Vec facing = target == null ? vex.getVelocity() : Vec.fromPoint(target.getPosition().sub(vex.getPosition()));
        vex.setView((float) Math.toDegrees(-Math.atan2(facing.x(), facing.z())), vex.getPosition().pitch());
    }
}

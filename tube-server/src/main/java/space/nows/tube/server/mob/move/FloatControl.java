package space.nows.tube.server.mob.move;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

public final class FloatControl implements Trait {

    private static final double PUSH = 5.0 / 3.0;
    private static final double TICKS_PER_SECOND = 20;
    private static final double SAMPLE = 0.5;

    private final Creature creature;
    private Point wanted;
    private int floatFor;

    public FloatControl(Creature creature) {
        this.creature = creature;
        creature.setNoGravity(true);
    }

    public Point wanted() {
        return wanted;
    }

    public void moveTo(Point point) {
        wanted = point;
    }

    public void stop() {
        wanted = null;
    }

    @Override
    public void tick() {
        if (wanted == null || floatFor-- > 0) {
            return;
        }
        floatFor += creature.random().nextInt(5) + 2;
        Vec travel = Vec.fromPoint(wanted.sub(creature.getPosition()));
        if (travel.lengthSquared() < 1.0E-6) {
            return;
        }
        if (!clear(travel)) {
            wanted = null;
            return;
        }
        double speed = creature.getAttributeValue(Attribute.FLYING_SPEED) * PUSH;
        creature.setVelocity(creature.getVelocity().add(travel.normalize().mul(speed * TICKS_PER_SECOND)));
    }

    private boolean clear(Vec travel) {
        Instance instance = creature.getInstance();
        double width = creature.getBoundingBox().width() / 2;
        double height = creature.getBoundingBox().height();
        int steps = (int) Math.ceil(travel.length() / SAMPLE);
        for (int i = 1; i <= steps; i++) {
            Point at = creature.getPosition().add(travel.mul((double) i / steps));
            for (double dx : new double[]{-width, width}) {
                for (double dz : new double[]{-width, width}) {
                    for (double dy : new double[]{0, height / 2, height}) {
                        Point corner = at.add(dx, dy, dz);
                        if (!instance.isChunkLoaded(corner) || instance.getBlock(corner).solid()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}

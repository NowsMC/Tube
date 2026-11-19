package space.nows.tube.server.mob.move;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.pathfinding.followers.GroundNodeFollower;
import net.minestom.server.entity.pathfinding.followers.NodeFollower;

final class Stride implements NodeFollower {

    private static final double GROUND_FRICTION = 0.6 * 0.91;
    private static final double GROUND_SETTLE = 1 / (1 - GROUND_FRICTION);
    private static final double WATER_FRICTION = 0.8;
    private static final double WATER_ACCELERATION = 0.02;
    private static final double WATER_SETTLE = 1 / (1 - WATER_FRICTION);

    private final LivingEntity entity;
    private final Navigation navigation;
    private final GroundNodeFollower ground;
    private double current;
    private long lastTick = Long.MIN_VALUE;

    Stride(LivingEntity entity, Navigation navigation) {
        this.entity = entity;
        this.navigation = navigation;
        this.ground = new GroundNodeFollower(entity);
    }

    static double perTick(double speed, boolean inWater) {
        return inWater ? WATER_ACCELERATION * speed * WATER_SETTLE : speed * speed * GROUND_SETTLE;
    }

    @Override
    public void moveTowards(Point target, double speed, Point lookAt) {
        ground.moveTowards(target, speed, lookAt);
    }

    @Override
    public void jump(Point point, Point target) {
        ground.jump(point, target);
    }

    @Override
    public boolean isAtPoint(Point point) {
        return ground.isAtPoint(point);
    }

    @Override
    public double movementSpeed() {
        double speed = entity.getAttributeValue(Attribute.MOVEMENT_SPEED) * navigation.speed();
        boolean inWater = navigation.inWater();
        long tick = entity.getAliveTicks();
        if (tick == lastTick) {
            return current;
        }
        if (tick - lastTick > 1) {
            current = 0;
        }
        lastTick = tick;
        double friction = inWater ? WATER_FRICTION : GROUND_FRICTION;
        current = current * friction + perTick(speed, inWater) * (1 - friction);
        return current;
    }
}

package space.nows.tube.server.mob.move;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.entity.pathfinding.PPath;
import net.minestom.server.instance.Instance;

public final class Navigation {

    private static final double POINT_REACH = 1.0;

    private final EntityCreature creature;
    private final Navigator navigator;
    private double speed = 1.0;

    public Navigation(EntityCreature creature) {
        this.creature = creature;
        this.navigator = creature.getNavigator();
        navigator.setNodeFollower(() -> new Stride(creature, this));
        navigator.setNodeGenerator(LoadedGround::new);
    }

    public boolean moveTo(Point target, double speed) {
        return start(target, POINT_REACH, speed);
    }

    public boolean moveTo(Entity target, double speed) {
        return start(target.getPosition(), 0, speed);
    }

    public void stop() {
        navigator.reset();
    }

    public boolean isDone() {
        PPath.State state = navigator.getState();
        return state == PPath.State.INVALID || state == PPath.State.TERMINATED || state == PPath.State.TERMINATING;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public Point destination() {
        return navigator.getGoalPosition();
    }

    double speed() {
        return speed;
    }

    boolean inWater() {
        Instance instance = creature.getInstance();
        return instance != null && Liquids.isWater(instance.getBlock(creature.getPosition()));
    }

    private boolean start(Point target, double reach, double speed) {
        this.speed = speed;
        return navigator.setPathTo(target, Math.max(reach, bodyRadius()), null);
    }

    private double bodyRadius() {
        BoundingBox box = creature.getBoundingBox();
        return Math.sqrt(box.width() * box.width() + box.depth() * box.depth()) / 2;
    }
}

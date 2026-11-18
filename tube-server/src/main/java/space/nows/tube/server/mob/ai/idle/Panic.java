package space.nows.tube.server.mob.ai.idle;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.ai.Control;
import space.nows.tube.server.mob.ai.Goal;
import space.nows.tube.server.mob.move.Liquids;
import space.nows.tube.server.mob.move.RandomSpot;

public final class Panic extends Goal {

    private static final int WATER_SEARCH = 5;

    private final Creature creature;
    private final double speed;
    private Point wanted;

    public Panic(Creature creature, double speed) {
        super(Control.MOVE);
        this.creature = creature;
        this.speed = speed;
    }

    @Override
    public boolean canStart() {
        if (!creature.harm().panicking(creature.getAliveTicks())) {
            return false;
        }
        if (creature.getFireTicks() > 0) {
            Point water = nearestWater();
            if (water != null) {
                wanted = water;
                return true;
            }
        }
        wanted = RandomSpot.anywhere(creature, 5, 4);
        return wanted != null;
    }

    @Override
    public boolean canContinue() {
        return !creature.navigation().isDone();
    }

    @Override
    public void start() {
        creature.navigation().moveTo(wanted, speed);
    }

    private Point nearestWater() {
        Instance instance = creature.getInstance();
        Point origin = creature.getPosition();
        if (instance.getBlock(origin).solid()) {
            return null;
        }
        Point best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -WATER_SEARCH; dx <= WATER_SEARCH; dx++) {
                for (int dz = -WATER_SEARCH; dz <= WATER_SEARCH; dz++) {
                    Point at = new Vec(origin.blockX() + dx, origin.blockY() + dy, origin.blockZ() + dz);
                    double distance = at.distanceSquared(new Vec(origin.blockX(), origin.blockY(), origin.blockZ()));
                    if (distance < bestDistance && Liquids.isWater(instance.getBlock(at))) {
                        bestDistance = distance;
                        best = at;
                    }
                }
            }
        }
        return best;
    }
}

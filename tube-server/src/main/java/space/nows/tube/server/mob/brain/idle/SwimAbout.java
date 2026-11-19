package space.nows.tube.server.mob.brain.idle;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.Liquids;

public final class SwimAbout extends Behavior {

    private static final int INTERVAL = 40;
    private static final int RANGE = 6;
    private static final int DEPTH_RANGE = 3;
    private static final int ATTEMPTS = 8;
    private static final int MAX_TICKS = 200;
    private static final double ARRIVED = 0.7;
    private static final double TICKS_PER_SECOND = 20;

    private final Creature creature;
    private final double speed;
    private Point wanted;
    private int swum;

    public SwimAbout(Creature creature, double blocksPerTick) {
        super(Control.MOVE);
        this.creature = creature;
        this.speed = blocksPerTick * TICKS_PER_SECOND;
    }

    @Override
    public boolean canStart() {
        if (!creature.inWater() || creature.random().nextInt(halved(INTERVAL)) != 0) {
            return false;
        }
        wanted = pick();
        return wanted != null;
    }

    @Override
    public boolean canContinue() {
        return wanted != null && creature.inWater() && swum < MAX_TICKS && creature.getPosition().distance(wanted) > ARRIVED;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void start() {
        swum = 0;
    }

    @Override
    public void stop() {
        wanted = null;
        creature.setVelocity(creature.getVelocity().mul(0.2));
    }

    @Override
    public void tick() {
        swum++;
        Pos at = creature.getPosition();
        Vec heading = new Vec(wanted.x() - at.x(), wanted.y() - at.y(), wanted.z() - at.z());
        if (heading.length() < 1.0E-4) {
            return;
        }
        Vec direction = heading.normalize();
        creature.setView((float) Math.toDegrees(Math.atan2(-direction.x(), direction.z())), (float) -Math.toDegrees(Math.asin(direction.y())));
        creature.setVelocity(direction.mul(speed));
    }

    private Point pick() {
        Instance instance = creature.getInstance();
        Pos at = creature.getPosition();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int x = at.blockX() + creature.random().nextInt(-RANGE, RANGE + 1);
            int y = at.blockY() + creature.random().nextInt(-DEPTH_RANGE, DEPTH_RANGE + 1);
            int z = at.blockZ() + creature.random().nextInt(-RANGE, RANGE + 1);
            if (Liquids.isWater(instance.getBlock(x, y, z)) && Liquids.isWater(instance.getBlock(x, y + 1, z))) {
                return new Vec(x + 0.5, y + 0.5, z + 0.5);
            }
        }
        return null;
    }
}

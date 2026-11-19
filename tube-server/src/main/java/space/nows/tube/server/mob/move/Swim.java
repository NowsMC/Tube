package space.nows.tube.server.mob.move;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;

public final class Swim {

    private static final double TICKS_PER_SECOND = 20;
    private static final int ATTEMPTS = 8;

    private Swim() {
    }

    public static void steer(Creature creature, Point target, double blocksPerTick) {
        Pos at = creature.getPosition();
        Vec heading = new Vec(target.x() - at.x(), target.y() - at.y(), target.z() - at.z());
        if (heading.length() < 1.0E-4) {
            return;
        }
        Vec direction = heading.normalize();
        creature.setView((float) Math.toDegrees(Math.atan2(-direction.x(), direction.z())), (float) -Math.toDegrees(Math.asin(direction.y())));
        creature.setVelocity(direction.mul(blocksPerTick * TICKS_PER_SECOND));
    }

    public static Point spot(Creature creature, int range, int depth) {
        return away(creature, null, range, depth);
    }

    public static Point away(Creature creature, Point threat, int range, int depth) {
        Instance instance = creature.getInstance();
        Pos at = creature.getPosition();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int x = at.blockX() + creature.random().nextInt(-range, range + 1);
            int y = at.blockY() + creature.random().nextInt(-depth, depth + 1);
            int z = at.blockZ() + creature.random().nextInt(-range, range + 1);
            Point candidate = new Vec(x + 0.5, y + 0.5, z + 0.5);
            if (!instance.isChunkLoaded(candidate) || !Liquids.isWater(instance.getBlock(x, y, z)) || !Liquids.isWater(instance.getBlock(x, y + 1, z))) {
                continue;
            }
            if (threat == null || candidate.distanceSquared(threat) > at.distanceSquared(threat)) {
                return candidate;
            }
        }
        return null;
    }
}

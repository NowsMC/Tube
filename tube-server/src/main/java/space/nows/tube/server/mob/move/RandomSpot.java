package space.nows.tube.server.mob.move;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;

import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public final class RandomSpot {

    private static final int ATTEMPTS = 10;
    private static final double QUARTER_TURN = Math.PI / 2;

    private RandomSpot() {
    }

    public static Point anywhere(Creature creature, int horizontal, int vertical) {
        return best(creature, () -> accepted(creature, offset(creature, randomDirection(creature.random(), horizontal, vertical)), true));
    }

    public static Point awayFrom(Creature creature, int horizontal, int vertical, Point danger) {
        Vec away = between(danger, creature.getPosition());
        return best(creature, () -> {
            Vec direction = directionWithin(creature.random(), 0, horizontal, vertical, away.x(), away.z());
            return direction == null ? null : accepted(creature, offset(creature, direction), true);
        });
    }

    public static Point onLand(Creature creature, int horizontal, int vertical) {
        return best(creature, () -> lifted(creature, accepted(creature, offset(creature, randomDirection(creature.random(), horizontal, vertical)), false)));
    }

    public static Point onLandAwayFrom(Creature creature, int horizontal, int vertical, Point danger) {
        Vec away = between(danger, creature.getPosition());
        if (away.length() == 0) {
            away = new Vec(creature.random().nextDouble() - 0.5, 0, creature.random().nextDouble() - 0.5);
        }
        Vec direction = away;
        return best(creature, () -> {
            Vec offset = directionWithin(creature.random(), 0, horizontal, vertical, direction.x(), direction.z());
            return offset == null ? null : lifted(creature, accepted(creature, offset(creature, offset), false));
        });
    }

    public static Point onLandTowards(Creature creature, int horizontal, int vertical, Point goal) {
        Vec towards = between(creature.getPosition(), goal);
        return best(creature, () -> {
            Vec offset = directionWithin(creature.random(), 0, horizontal, vertical, towards.x(), towards.z());
            return offset == null ? null : lifted(creature, accepted(creature, offset(creature, offset), false));
        });
    }

    private static Vec between(Point from, Point to) {
        return new Vec(to.x() - from.x(), to.y() - from.y(), to.z() - from.z());
    }

    private static Point best(Creature creature, Supplier<Point> candidates) {
        double bestWeight = Double.NEGATIVE_INFINITY;
        Point best = null;
        for (int i = 0; i < ATTEMPTS; i++) {
            Point candidate = candidates.get();
            if (candidate != null) {
                double weight = creature.walkPreference(candidate);
                if (weight > bestWeight) {
                    bestWeight = weight;
                    best = candidate;
                }
            }
        }
        return best == null ? null : new Vec(best.blockX() + 0.5, best.blockY(), best.blockZ() + 0.5);
    }

    private static Vec randomDirection(RandomGenerator random, int horizontal, int vertical) {
        return new Vec(random.nextInt(2 * horizontal + 1) - horizontal, random.nextInt(2 * vertical + 1) - vertical,
                random.nextInt(2 * horizontal + 1) - horizontal);
    }

    private static Vec directionWithin(RandomGenerator random, double minHorizontal, double maxHorizontal, int vertical, double dirX, double dirZ) {
        double center = Math.atan2(dirZ, dirX) - QUARTER_TURN;
        double yaw = center + (2 * random.nextFloat() - 1) * QUARTER_TURN;
        double distance = (minHorizontal + Math.sqrt(random.nextDouble()) * (maxHorizontal - minHorizontal)) * Math.sqrt(2);
        double x = -distance * Math.sin(yaw);
        double z = distance * Math.cos(yaw);
        if (Math.abs(x) > maxHorizontal || Math.abs(z) > maxHorizontal) {
            return null;
        }
        return new Vec(x, random.nextInt(2 * vertical + 1) - vertical, z);
    }

    private static Point offset(Creature creature, Vec direction) {
        Point position = creature.getPosition();
        return new Vec(Math.floor(position.x() + direction.x()), Math.floor(position.y() + direction.y()), Math.floor(position.z() + direction.z()));
    }

    private static Point accepted(Creature creature, Point at, boolean checkPenalty) {
        Instance instance = creature.getInstance();
        if (instance == null || at.blockY() < instance.getCachedDimensionType().minY() || at.blockY() >= instance.getCachedDimensionType().maxY()
                || !instance.isChunkLoaded(at)) {
            return null;
        }
        if (!Footing.stable(instance, at) || checkPenalty && Footing.penalized(instance, at)) {
            return null;
        }
        return at;
    }

    private static Point lifted(Creature creature, Point at) {
        if (at == null) {
            return null;
        }
        Instance instance = creature.getInstance();
        if (!instance.isChunkLoaded(at)) {
            return null;
        }
        int maxY = instance.getCachedDimensionType().maxY();
        Point up = at;
        while (up.blockY() <= maxY && Footing.solid(instance, up)) {
            up = up.add(0, 1, 0);
        }
        return Footing.water(instance, up) || Footing.penalized(instance, up) ? null : up;
    }
}

package space.nows.tube.server.command.builtin.entity.spread;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

public final class SpreadPlanner {

    private static final int ATTEMPTS_PER_POINT = 200;

    private final RandomGenerator random;

    public SpreadPlanner(RandomGenerator random) {
        this.random = random;
    }

    public Optional<List<SpreadPoint>> plan(double centerX, double centerZ, double spreadDistance, double maxRange, int count,
                                            SpreadFilter filter) {
        List<SpreadPoint> placed = new ArrayList<>(count);
        double minimum = spreadDistance * spreadDistance;
        for (int i = 0; i < count; i++) {
            SpreadPoint point = pick(centerX, centerZ, maxRange, minimum, placed, filter);
            if (point == null) {
                return Optional.empty();
            }
            placed.add(point);
        }
        return Optional.of(placed);
    }

    private SpreadPoint pick(double centerX, double centerZ, double maxRange, double minimum, List<SpreadPoint> placed, SpreadFilter filter) {
        for (int attempt = 0; attempt < ATTEMPTS_PER_POINT; attempt++) {
            SpreadPoint candidate = new SpreadPoint(
                    Math.floor(centerX + (random.nextDouble() * 2 - 1) * maxRange) + 0.5,
                    Math.floor(centerZ + (random.nextDouble() * 2 - 1) * maxRange) + 0.5);
            if (placed.stream().noneMatch(other -> other.distanceSquared(candidate) < minimum) && filter.accepts(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}

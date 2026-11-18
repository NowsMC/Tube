package space.nows.tube.server.command.builtin.entity.spread;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

class SpreadPlannerTest {

    private final SpreadPlanner planner = new SpreadPlanner(RandomGenerator.of("L64X128MixRandom"));

    @Test
    void keepsPointsApartAndInsideTheRange() {
        List<SpreadPoint> points = planner.plan(100, -50, 10, 60, 8, point -> true).orElseThrow();

        assertEquals(8, points.size());
        for (int i = 0; i < points.size(); i++) {
            assertTrue(Math.abs(points.get(i).x() - 100) <= 61 && Math.abs(points.get(i).z() + 50) <= 61);
            for (int j = i + 1; j < points.size(); j++) {
                assertTrue(points.get(i).distanceSquared(points.get(j)) >= 100);
            }
        }
    }

    @Test
    void givesUpWhenThereIsNoRoom() {
        assertEquals(Optional.empty(), planner.plan(0, 0, 50, 51, 10, point -> true));
    }

    @Test
    void givesUpWhenNoGroundIsAcceptable() {
        assertEquals(Optional.empty(), planner.plan(0, 0, 1, 10, 1, point -> false));
    }
}

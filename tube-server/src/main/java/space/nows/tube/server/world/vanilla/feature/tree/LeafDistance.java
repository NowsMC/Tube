package space.nows.tube.server.world.vanilla.feature.tree;

import space.nows.tube.server.world.vanilla.block.BlockState;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

final class LeafDistance {

    private static final int MAX = 6;
    private static final int[][] NEIGHBOURS = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    private LeafDistance() {
    }

    static void update(TreeGrowth growth) {
        Map<Long, Integer> distances = measure(growth);
        for (long leaf : growth.leafPositions()) {
            Integer distance = distances.get(leaf);
            if (distance == null) {
                continue;
            }
            int x = Packed.x(leaf);
            int y = Packed.y(leaf);
            int z = Packed.z(leaf);
            BlockState state = growth.area.get(x, y, z);
            if (state.hasProperty("distance")) {
                growth.area.set(x, y, z, state.with("distance", Integer.toString(distance)));
            }
        }
    }

    private static Map<Long, Integer> measure(TreeGrowth growth) {
        Map<Long, Integer> distances = new HashMap<>();
        ArrayDeque<Long> frontier = new ArrayDeque<>();
        for (long log : growth.logPositions()) {
            distances.put(log, 0);
            frontier.add(log);
        }
        while (!frontier.isEmpty()) {
            long position = frontier.poll();
            int distance = distances.get(position);
            if (distance >= MAX) {
                continue;
            }
            for (int[] offset : NEIGHBOURS) {
                long next = Packed.of(Packed.x(position) + offset[0], Packed.y(position) + offset[1], Packed.z(position) + offset[2]);
                if (growth.leafPositions().contains(next) && !distances.containsKey(next)) {
                    distances.put(next, distance + 1);
                    frontier.add(next);
                }
            }
        }
        return distances;
    }
}

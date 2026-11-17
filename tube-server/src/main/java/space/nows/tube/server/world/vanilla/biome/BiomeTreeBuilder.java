package space.nows.tube.server.world.vanilla.biome;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class BiomeTreeBuilder {

    private static final int FAN_OUT = 6;

    private BiomeTreeBuilder() {
    }

    static BiomeTree.Node build(List<BiomeTree.Node> nodes) {
        if (nodes.size() == 1) {
            return nodes.getFirst();
        }
        if (nodes.size() <= FAN_OUT) {
            List<BiomeTree.Node> sorted = new ArrayList<>(nodes);
            sorted.sort(Comparator.comparingLong(BiomeTreeBuilder::distanceFromOrigin));
            return new BiomeTree.Branch(sorted);
        }
        Grouping grouping = cheapestGrouping(nodes);
        grouping.groups.sort(byCenter(grouping.axis, true));
        List<BiomeTree.Node> children = new ArrayList<>();
        for (BiomeTree.Branch group : grouping.groups) {
            children.add(build(List.of(group.children)));
        }
        return new BiomeTree.Branch(children);
    }

    private record Grouping(int axis, List<BiomeTree.Branch> groups) {
    }

    private static Grouping cheapestGrouping(List<BiomeTree.Node> nodes) {
        long bestCost = Long.MAX_VALUE;
        Grouping best = null;
        for (int axis = 0; axis < ClimateSpot.AXES; axis++) {
            List<BiomeTree.Node> sorted = new ArrayList<>(nodes);
            sorted.sort(byCenter(axis, false));
            List<BiomeTree.Branch> groups = group(sorted);
            long cost = groups.stream().mapToLong(BiomeTree.Node::size).sum();
            if (cost < bestCost) {
                bestCost = cost;
                best = new Grouping(axis, groups);
            }
        }
        return best;
    }

    private static List<BiomeTree.Branch> group(List<BiomeTree.Node> sorted) {
        int groupSize = (int) Math.pow(FAN_OUT, Math.floor(Math.log(sorted.size() - 0.01) / Math.log(FAN_OUT)));
        List<BiomeTree.Branch> groups = new ArrayList<>();
        for (int start = 0; start < sorted.size(); start += groupSize) {
            groups.add(new BiomeTree.Branch(sorted.subList(start, Math.min(sorted.size(), start + groupSize))));
        }
        return groups;
    }

    private static long distanceFromOrigin(BiomeTree.Node node) {
        long total = 0;
        for (int axis = 0; axis < ClimateSpot.AXES; axis++) {
            total += Math.abs(node.center(axis));
        }
        return total;
    }

    private static <T extends BiomeTree.Node> Comparator<T> byCenter(int axis, boolean absolute) {
        Comparator<T> comparator = Comparator.comparingLong(node -> center(node, axis, absolute));
        for (int i = 1; i < ClimateSpot.AXES; i++) {
            int next = (axis + i) % ClimateSpot.AXES;
            comparator = comparator.thenComparingLong(node -> center(node, next, absolute));
        }
        return comparator;
    }

    private static long center(BiomeTree.Node node, int axis, boolean absolute) {
        long center = node.center(axis);
        return absolute ? Math.abs(center) : center;
    }
}

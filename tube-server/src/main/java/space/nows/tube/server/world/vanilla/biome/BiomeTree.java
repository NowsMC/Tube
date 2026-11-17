package space.nows.tube.server.world.vanilla.biome;

import java.util.ArrayList;
import java.util.List;

public final class BiomeTree {

    abstract static sealed class Node permits Leaf, Branch {
        final long[] bounds;

        Node(long[] bounds) {
            this.bounds = bounds;
        }

        long distanceTo(long[] target) {
            long total = 0;
            for (int axis = 0; axis < ClimateSpot.AXES; axis++) {
                long value = axis < target.length ? target[axis] : 0;
                long above = value - bounds[axis * 2 + 1];
                long below = bounds[axis * 2] - value;
                long gap = above > 0 ? above : Math.max(below, 0);
                total += gap * gap;
            }
            return total;
        }

        long center(int axis) {
            return (bounds[axis * 2] + bounds[axis * 2 + 1]) / 2;
        }

        long size() {
            long size = 0;
            for (int axis = 0; axis < ClimateSpot.AXES; axis++) {
                size += Math.abs(bounds[axis * 2 + 1] - bounds[axis * 2]);
            }
            return size;
        }
    }

    static final class Leaf extends Node {
        final String biome;

        Leaf(ClimateSpot spot) {
            super(spot.bounds());
            this.biome = spot.biome();
        }
    }

    static final class Branch extends Node {
        final Node[] children;

        Branch(List<? extends Node> children) {
            super(enclose(children));
            this.children = children.toArray(Node[]::new);
        }

        private static long[] enclose(List<? extends Node> children) {
            long[] bounds = new long[ClimateSpot.AXES * 2];
            for (int axis = 0; axis < ClimateSpot.AXES; axis++) {
                bounds[axis * 2] = Long.MAX_VALUE;
                bounds[axis * 2 + 1] = Long.MIN_VALUE;
            }
            for (Node child : children) {
                for (int axis = 0; axis < ClimateSpot.AXES; axis++) {
                    bounds[axis * 2] = Math.min(bounds[axis * 2], child.bounds[axis * 2]);
                    bounds[axis * 2 + 1] = Math.max(bounds[axis * 2 + 1], child.bounds[axis * 2 + 1]);
                }
            }
            return bounds;
        }
    }

    private final Node root;

    public BiomeTree(List<ClimateSpot> spots) {
        List<Node> leaves = new ArrayList<>();
        spots.forEach(spot -> leaves.add(new Leaf(spot)));
        this.root = BiomeTreeBuilder.build(leaves);
    }

    public Cursor cursor() {
        return new Cursor();
    }

    public final class Cursor {
        private Leaf last;

        public String closest(long[] target) {
            long bestDistance = last == null ? Long.MAX_VALUE : last.distanceTo(target);
            last = closest(root, target, last, bestDistance);
            return last.biome;
        }

        private Leaf closest(Node node, long[] target, Leaf best, long bestDistance) {
            if (node instanceof Leaf leaf) {
                return leaf;
            }
            for (Node child : ((Branch) node).children) {
                long distance = child.distanceTo(target);
                if (distance >= bestDistance) {
                    continue;
                }
                Leaf found = closest(child, target, best, bestDistance);
                long foundDistance = found == child ? distance : found.distanceTo(target);
                if (foundDistance < bestDistance) {
                    bestDistance = foundDistance;
                    best = found;
                }
            }
            return best;
        }
    }
}

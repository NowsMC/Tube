package space.nows.tube.server.world.vanilla.feature.tree;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.choice.BlockChoice;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class TreeGrowth {

    public final DecorationArea area;
    public final Xoroshiro random;
    private final BlockChoice wood;
    private final BlockChoice foliage;
    private final BlockChoice soil;
    private final List<int[]> logs = new ArrayList<>();
    private final Set<Long> logPositions = new LinkedHashSet<>();
    private final Set<Long> leafPositions = new LinkedHashSet<>();

    TreeGrowth(DecorationArea area, Xoroshiro random, BlockChoice wood, BlockChoice foliage, BlockChoice soil) {
        this.area = area;
        this.random = random;
        this.wood = wood;
        this.foliage = foliage;
        this.soil = soil;
    }

    public boolean canGrowInto(int x, int y, int z) {
        BlockState state = area.get(x, y, z);
        return state.air || state.is("minecraft:replaceable_by_trees");
    }

    public boolean isFree(int x, int y, int z) {
        return canGrowInto(x, y, z) || area.get(x, y, z).is("minecraft:logs");
    }

    public boolean log(int x, int y, int z) {
        return log(x, y, z, null);
    }

    public boolean log(int x, int y, int z, String axis) {
        if (area.outsideHeight(y) || !isFree(x, y, z)) {
            return false;
        }
        BlockState state = wood.choose(random, area, x, y, z);
        if (state == null) {
            return false;
        }
        area.set(x, y, z, axis == null ? state : state.with("axis", axis));
        logs.add(new int[]{x, y, z});
        logPositions.add(Packed.of(x, y, z));
        return true;
    }

    public void leaf(int x, int y, int z) {
        if (area.outsideHeight(y) || !canGrowInto(x, y, z)) {
            return;
        }
        BlockState state = foliage.choose(random, area, x, y, z);
        if (state == null) {
            return;
        }
        if (area.get(x, y, z).water && state.hasProperty("waterlogged")) {
            state = state.with("waterlogged", "true");
        }
        area.set(x, y, z, state);
        leafPositions.add(Packed.of(x, y, z));
    }

    public void soil(int x, int y, int z) {
        BlockState state = soil.choose(random, area, x, y, z);
        if (state != null) {
            area.set(x, y, z, state);
        }
    }

    public List<int[]> logs() {
        return logs;
    }

    public Set<Long> logPositions() {
        return logPositions;
    }

    public Set<Long> leafPositions() {
        return leafPositions;
    }

    boolean grewAnything() {
        return !logs.isEmpty() || !leafPositions.isEmpty();
    }
}

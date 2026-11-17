package space.nows.tube.server.world.vanilla.feature.tree;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.feature.choice.BlockChoice;
import space.nows.tube.server.world.vanilla.feature.tree.canopy.Canopy;
import space.nows.tube.server.world.vanilla.feature.tree.decorator.TreeDecorator;
import space.nows.tube.server.world.vanilla.feature.tree.decorator.TreeDecorators;
import space.nows.tube.server.world.vanilla.feature.tree.trunk.Trunk;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

public final class TreeFeature implements Feature {

    private static final BlockChoice PLAIN_DIRT = (random, area, x, y, z) -> area.get(x, y, z).is("minecraft:dirt") ? null : BlockStates.of("dirt");

    private final BlockChoice wood;
    private final BlockChoice foliage;
    private final BlockChoice soil;
    private final Trunk trunk;
    private final Canopy canopy;
    private final TreeSize size;
    private final boolean ignoresVines;
    private final List<TreeDecorator> decorators = new ArrayList<>();

    public TreeFeature(JsonObject config) {
        this.wood = BlockChoice.read(config.get("trunk_provider"));
        this.foliage = BlockChoice.read(config.get("foliage_provider"));
        this.soil = config.has("below_trunk_provider") ? BlockChoice.read(config.get("below_trunk_provider")) : PLAIN_DIRT;
        this.trunk = Trunk.read(config.getAsJsonObject("trunk_placer"));
        this.canopy = Canopy.read(config.getAsJsonObject("foliage_placer"));
        this.size = TreeSize.read(config.getAsJsonObject("minimum_size"));
        this.ignoresVines = config.has("ignore_vines") && config.get("ignore_vines").getAsBoolean();
        if (config.has("decorators")) {
            for (JsonElement element : config.getAsJsonArray("decorators")) {
                TreeDecorators.read(element.getAsJsonObject()).ifPresent(decorators::add);
            }
        }
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        TreeGrowth growth = new TreeGrowth(area, random, wood, foliage, soil);
        int height = trunk.height(random);
        int crownHeight = canopy.crownHeight(random, height);
        int crownRadius = canopy.crownRadius(random, height - crownHeight);
        if (y < area.minY + 1 || y + height + 1 > area.maxY) {
            return false;
        }
        int room = freeHeight(growth, height, x, y, z);
        if (room < height && !size.acceptsClipped(room)) {
            return false;
        }
        for (Attachment crown : trunk.grow(growth, random, room, x, y, z)) {
            canopy.grow(growth, random, crown, crownHeight, crownRadius);
        }
        if (!growth.grewAnything()) {
            return false;
        }
        decorators.forEach(decorator -> decorator.decorate(growth));
        LeafDistance.update(growth);
        return true;
    }

    private int freeHeight(TreeGrowth growth, int height, int x, int y, int z) {
        for (int dy = 0; dy <= height + 1; dy++) {
            int clearance = size.clearanceAt(height, dy);
            for (int dx = -clearance; dx <= clearance; dx++) {
                for (int dz = -clearance; dz <= clearance; dz++) {
                    if (!growth.isFree(x + dx, y + dy, z + dz) || !ignoresVines && isVine(growth, x + dx, y + dy, z + dz)) {
                        return dy - 2;
                    }
                }
            }
        }
        return height;
    }

    private static boolean isVine(TreeGrowth growth, int x, int y, int z) {
        return growth.area.get(x, y, z).isBlock("minecraft:vine");
    }
}

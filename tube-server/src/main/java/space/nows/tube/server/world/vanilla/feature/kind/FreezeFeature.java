package space.nows.tube.server.world.vanilla.feature.kind;

import space.nows.tube.server.world.vanilla.biome.BiomeInfo;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.block.Heightmap;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.feature.test.PlantSupport;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

public record FreezeFeature() implements Feature {

    private static final BlockState ICE = BlockStates.of("ice");
    private static final BlockState SNOW = BlockStates.of("snow");

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                freezeColumn(area, x + dx, z + dz);
            }
        }
        return true;
    }

    private static void freezeColumn(DecorationArea area, int x, int z) {
        int top = area.height(Heightmap.SOLID_OR_FLUID, x, z);
        BiomeInfo biome = area.biome(x, top, z);
        if (biome == null) {
            return;
        }
        BlockState below = area.get(x, top - 1, z);
        if (biome.snowsAt(x, top - 1, z, area.seaLevel) && below.isBlock("minecraft:water") && below.isSource()) {
            area.set(x, top - 1, z, ICE);
        }
        if (biome.snowsAt(x, top, z, area.seaLevel) && !area.outsideHeight(top) && area.isAir(x, top, z)
                && PlantSupport.canStay(area, SNOW, x, top, z)) {
            area.set(x, top, z, SNOW);
            BlockState ground = area.get(x, top - 1, z);
            if (ground.hasProperty("snowy")) {
                area.set(x, top - 1, z, ground.with("snowy", "true"));
            }
        }
    }
}

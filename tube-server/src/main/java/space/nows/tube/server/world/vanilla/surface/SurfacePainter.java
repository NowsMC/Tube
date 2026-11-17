package space.nows.tube.server.world.vanilla.surface;

import space.nows.tube.server.world.vanilla.biome.BiomeInfo;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.Heightmap;
import space.nows.tube.server.world.vanilla.noise.PairedNoise;
import space.nows.tube.server.world.vanilla.noise.WorldNoises;
import space.nows.tube.server.world.vanilla.random.PositionalRandom;
import space.nows.tube.server.world.vanilla.terrain.ChunkDensity;
import space.nows.tube.server.world.vanilla.terrain.ChunkDraft;
import space.nows.tube.server.world.vanilla.terrain.TerrainSettings;

import java.util.Map;

public final class SurfacePainter {

    private static final int NO_CEILING = -2_032_128;

    private final BlockState stone;
    private final SurfaceRule rule;
    private final PairedNoise depthNoise;
    private final PairedNoise secondaryNoise;
    private final PositionalRandom depthJitter;

    public SurfacePainter(TerrainSettings settings, WorldNoises noises, Map<String, BiomeInfo> biomes) {
        this.stone = settings.stone();
        this.depthNoise = noises.noise("minecraft:surface");
        this.secondaryNoise = noises.noise("minecraft:surface_secondary");
        this.depthJitter = noises.positional("minecraft:surface");
        ClayBands bands = new ClayBands(noises.named("minecraft:clay_bands"), noises.noise("minecraft:clay_bands_offset"));
        this.rule = new SurfaceRuleReader(new SurfaceConditionReader(settings, noises, biomes), bands).read(settings.surfaceRule());
    }

    public void paint(ChunkDraft draft, ChunkDensity density) {
        int[] tops = new int[256];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                tops[z << 4 | x] = draft.height(Heightmap.ANY_BLOCK, x, z);
            }
        }
        SurfaceColumn column = new SurfaceColumn(draft, density, tops, depthNoise, secondaryNoise, depthJitter);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                column.moveTo(x, z);
                paintColumn(draft, column, tops[z << 4 | x]);
            }
        }
    }

    private void paintColumn(ChunkDraft draft, SurfaceColumn column, int top) {
        int stoneAbove = 0;
        int waterTop = Integer.MIN_VALUE;
        int ceiling = Integer.MAX_VALUE;
        for (int y = top; y >= draft.minY; y--) {
            BlockState state = draft.get(column.localX, y, column.localZ);
            if (state.air) {
                stoneAbove = 0;
                waterTop = Integer.MIN_VALUE;
                continue;
            }
            if (state.hasFluid()) {
                if (waterTop == Integer.MIN_VALUE) {
                    waterTop = y + 1;
                }
                continue;
            }
            if (ceiling >= y) {
                ceiling = nextOpening(draft, column, y);
            }
            stoneAbove++;
            column.moveDown(y, stoneAbove, y - ceiling + 1, waterTop);
            if (state == stone) {
                BlockState painted = rule.paint(column);
                if (painted != null) {
                    draft.set(column.localX, y, column.localZ, painted);
                }
            }
        }
    }

    private static int nextOpening(ChunkDraft draft, SurfaceColumn column, int y) {
        for (int below = y - 1; below >= draft.minY - 1; below--) {
            BlockState state = draft.get(column.localX, below, column.localZ);
            if (state.air || state.hasFluid()) {
                return below + 1;
            }
        }
        return NO_CEILING;
    }
}

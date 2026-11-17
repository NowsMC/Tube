package space.nows.tube.server.world.vanilla.feature;

import space.nows.tube.server.world.vanilla.biome.BiomeInfo;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.block.Heightmap;
import space.nows.tube.server.world.vanilla.terrain.ChunkDraft;

import java.util.Arrays;
import java.util.Map;

public final class DecorationArea {

    public static final int CHUNKS_ACROSS = 3;
    private static final int BLOCKS_ACROSS = CHUNKS_ACROSS * 16;
    private static final Heightmap[] HEIGHTMAPS = Heightmap.values();
    private static final int UNKNOWN = Integer.MIN_VALUE;

    private final ChunkDraft[] drafts;
    private final int minChunkX;
    private final int minChunkZ;
    public final int minY;
    public final int maxY;
    public final int seaLevel;
    private final Map<String, BiomeInfo> biomes;
    private final int[][] heights = new int[HEIGHTMAPS.length][BLOCKS_ACROSS * BLOCKS_ACROSS];

    public DecorationArea(ChunkDraft[] drafts, int centerChunkX, int centerChunkZ, int seaLevel, Map<String, BiomeInfo> biomes) {
        this.drafts = drafts;
        this.minChunkX = centerChunkX - 1;
        this.minChunkZ = centerChunkZ - 1;
        this.minY = drafts[0].minY;
        this.maxY = drafts[0].maxY();
        this.seaLevel = seaLevel;
        this.biomes = biomes;
        for (int[] tops : heights) {
            Arrays.fill(tops, UNKNOWN);
        }
    }

    private ChunkDraft draftAt(int x, int z) {
        int column = (x >> 4) - minChunkX;
        int row = (z >> 4) - minChunkZ;
        if (column < 0 || column >= CHUNKS_ACROSS || row < 0 || row >= CHUNKS_ACROSS) {
            return null;
        }
        return drafts[row * CHUNKS_ACROSS + column];
    }

    public boolean outsideHeight(int y) {
        return y < minY || y >= maxY;
    }

    public BlockState get(int x, int y, int z) {
        ChunkDraft draft = draftAt(x, z);
        return draft == null ? BlockStates.AIR : draft.get(x & 15, y, z & 15);
    }

    public boolean isAir(int x, int y, int z) {
        return get(x, y, z).air;
    }

    public void set(int x, int y, int z, BlockState state) {
        ChunkDraft draft = draftAt(x, z);
        if (draft == null || outsideHeight(y)) {
            return;
        }
        draft.set(x & 15, y, z & 15, state);
        int column = column(x, z);
        for (Heightmap heightmap : HEIGHTMAPS) {
            int[] tops = heights[heightmap.ordinal()];
            int top = tops[column];
            if (top == UNKNOWN) {
                continue;
            }
            if (heightmap.counts(state)) {
                tops[column] = Math.max(top, y + 1);
            } else if (y + 1 == top) {
                tops[column] = UNKNOWN;
            }
        }
    }

    public int height(Heightmap heightmap, int x, int z) {
        ChunkDraft draft = draftAt(x, z);
        if (draft == null) {
            return minY;
        }
        int[] tops = heights[heightmap.ordinal()];
        int column = column(x, z);
        if (tops[column] == UNKNOWN) {
            tops[column] = draft.height(heightmap, x & 15, z & 15);
        }
        return tops[column];
    }

    private int column(int x, int z) {
        return (z - (minChunkZ << 4)) * BLOCKS_ACROSS + x - (minChunkX << 4);
    }

    public BiomeInfo biome(int x, int y, int z) {
        ChunkDraft draft = draftAt(x, z);
        return draft == null ? null : biomes.get(draft.biome(x & 15, y, z & 15));
    }
}

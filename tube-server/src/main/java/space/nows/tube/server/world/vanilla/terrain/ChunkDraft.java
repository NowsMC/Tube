package space.nows.tube.server.world.vanilla.terrain;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.block.Heightmap;

import java.util.Arrays;

public final class ChunkDraft {

    public final int chunkX;
    public final int chunkZ;
    public final int minY;
    public final int height;
    private final short[] blocks;
    private final String[] biomes;

    public ChunkDraft(int chunkX, int chunkZ, int minY, int height) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.minY = minY;
        this.height = height;
        this.blocks = new short[16 * 16 * height];
        this.biomes = new String[16 * (height >> 2)];
        Arrays.fill(blocks, (short) BlockStates.AIR.id());
    }

    private ChunkDraft(ChunkDraft source) {
        this.chunkX = source.chunkX;
        this.chunkZ = source.chunkZ;
        this.minY = source.minY;
        this.height = source.height;
        this.blocks = source.blocks.clone();
        this.biomes = source.biomes;
    }

    public ChunkDraft copy() {
        return new ChunkDraft(this);
    }

    public int minX() {
        return chunkX << 4;
    }

    public int minZ() {
        return chunkZ << 4;
    }

    public int maxY() {
        return minY + height;
    }

    public int quartHeight() {
        return height >> 2;
    }

    public BlockState get(int x, int y, int z) {
        if (y < minY || y >= maxY()) {
            return BlockStates.AIR;
        }
        return BlockStates.byId(blocks[index(x, y, z)]);
    }

    public void set(int x, int y, int z, BlockState state) {
        if (y >= minY && y < maxY()) {
            blocks[index(x, y, z)] = (short) state.id();
        }
    }

    private int index(int x, int y, int z) {
        return (y - minY) << 8 | z << 4 | x;
    }

    public String biome(int x, int y, int z) {
        int quartY = Math.clamp((y - minY) >> 2, 0, quartHeight() - 1);
        return quartBiome(x >> 2, quartY, z >> 2);
    }

    public String quartBiome(int quartX, int quartY, int quartZ) {
        return biomes[quartY << 4 | quartZ << 2 | quartX];
    }

    public void setQuartBiome(int quartX, int quartY, int quartZ, String biome) {
        biomes[quartY << 4 | quartZ << 2 | quartX] = biome;
    }

    public int height(Heightmap heightmap, int x, int z) {
        for (int y = maxY() - 1; y >= minY; y--) {
            if (heightmap.counts(get(x, y, z))) {
                return y + 1;
            }
        }
        return minY;
    }
}

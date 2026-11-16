package space.nows.tube.server.world;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.generator.GenerationUnit;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.ChunkBuilder;

final class MinestomChunkBuilder implements ChunkBuilder {

    private final GenerationUnit unit;
    private final int startX;
    private final int startZ;
    private final int minY;
    private final int maxY;

    MinestomChunkBuilder(GenerationUnit unit) {
        this.unit = unit;
        this.startX = unit.absoluteStart().blockX();
        this.startZ = unit.absoluteStart().blockZ();
        this.minY = unit.absoluteStart().blockY();
        this.maxY = unit.absoluteEnd().blockY();
    }

    @Override
    public int chunkX() {
        return startX >> 4;
    }

    @Override
    public int chunkZ() {
        return startZ >> 4;
    }

    @Override
    public int minY() {
        return minY;
    }

    @Override
    public int maxY() {
        return maxY;
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) {
        if (y >= minY && y < maxY) {
            unit.modifier().setBlock(startX + x, y, startZ + z, BlockMapper.toMinestom(block));
        }
    }

    @Override
    public void fillColumn(int x, int z, int fromY, int toY, Block block) {
        int from = Math.max(fromY, minY);
        int to = Math.min(toY, maxY);
        if (from < to) {
            unit.modifier().fill(new Vec(startX + x, from, startZ + z), new Vec(startX + x + 1, to, startZ + z + 1), BlockMapper.toMinestom(block));
        }
    }

    @Override
    public void fillLayers(int fromY, int toY, Block block) {
        int from = Math.max(fromY, minY);
        int to = Math.min(toY, maxY);
        if (from < to) {
            unit.modifier().fillHeight(from, to, BlockMapper.toMinestom(block));
        }
    }
}

package space.nows.tube.server;

import net.minestom.server.instance.InstanceContainer;
import space.nows.tube.api.Block;
import space.nows.tube.api.Location;
import space.nows.tube.api.World;

final class TubeWorld implements World {

    private final String name;
    private final InstanceContainer instance;
    private final double[] spawn;

    TubeWorld(String name, InstanceContainer instance, double[] spawn) {
        this.name = name;
        this.instance = instance;
        this.spawn = spawn;
    }

    InstanceContainer instance() {
        return instance;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Location spawn() {
        return new Location(this, spawn[0], spawn[1], spawn[2]);
    }

    @Override
    public Block block(int x, int y, int z) {
        instance.loadChunk(x >> 4, z >> 4).join();
        return BlockMapper.fromMinestom(instance.getBlock(x, y, z));
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) {
        net.minestom.server.instance.block.Block resolved = BlockMapper.toMinestom(block);
        instance.loadChunk(x >> 4, z >> 4).join();
        instance.setBlock(x, y, z, resolved);
    }

    @Override
    public void save() {
        instance.saveInstance().join();
        instance.saveChunksToStorage().join();
    }
}

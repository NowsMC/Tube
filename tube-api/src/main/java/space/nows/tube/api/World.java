package space.nows.tube.api;

public interface World {

    String name();

    Location spawn();

    Block block(int x, int y, int z);

    void setBlock(int x, int y, int z, Block block);

    void save();
}

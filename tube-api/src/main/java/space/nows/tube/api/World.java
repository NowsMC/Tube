package space.nows.tube.api;

import java.util.Collection;

public interface World {

    String name();

    Location spawn();

    Block block(int x, int y, int z);

    void setBlock(int x, int y, int z, Block block);

    Entity spawn(String type, Location location);

    Entity dropItem(Location location, ItemStack item);

    Collection<Entity> entities();

    void save();
}

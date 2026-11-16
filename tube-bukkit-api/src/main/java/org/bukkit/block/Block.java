package org.bukkit.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public interface Block {

    int getX();

    int getY();

    int getZ();

    World getWorld();

    Location getLocation();

    Material getType();

    void setType(Material type);

    void setType(Material type, boolean applyPhysics);

    Block getRelative(BlockFace face);

    Block getRelative(int dx, int dy, int dz);

    boolean isEmpty();

    boolean isLiquid();
}

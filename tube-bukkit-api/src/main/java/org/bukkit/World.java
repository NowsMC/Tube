package org.bukkit;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface World {

    String getName();

    UUID getUID();

    NamespacedKey getKey();

    Location getSpawnLocation();

    Environment getEnvironment();

    Block getBlockAt(int x, int y, int z);

    Block getBlockAt(Location location);

    List<Player> getPlayers();

    List<Entity> getEntities();

    int getMinHeight();

    int getMaxHeight();

    int getSeaLevel();

    void save();

    enum Environment {
        NORMAL,
        NETHER,
        THE_END,
        CUSTOM
    }
}

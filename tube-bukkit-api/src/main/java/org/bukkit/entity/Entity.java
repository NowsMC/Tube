package org.bukkit.entity;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public interface Entity extends CommandSender {

    Location getLocation();

    int getEntityId();

    World getWorld();

    EntityType getType();

    UUID getUniqueId();

    boolean teleport(Location location);

    String getCustomName();

    void setCustomName(String name);

    boolean isValid();

    boolean isDead();

    void remove();

    @Override
    Server getServer();

    java.util.Set<String> getScoreboardTags();

    boolean addScoreboardTag(String tag);

    boolean removeScoreboardTag(String tag);

    @Override
    default Spigot spigot() {
        return new Spigot();
    }

    class Spigot extends CommandSender.Spigot {
    }
}

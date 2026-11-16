package org.bukkit.event.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public class PlayerRespawnEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private Location respawnLocation;
    private final boolean bedSpawn;
    private final boolean anchorSpawn;

    public PlayerRespawnEvent(Player player, Location respawnLocation, boolean bedSpawn) {
        this(player, respawnLocation, bedSpawn, false);
    }

    public PlayerRespawnEvent(Player player, Location respawnLocation, boolean bedSpawn, boolean anchorSpawn) {
        super(player);
        this.respawnLocation = respawnLocation;
        this.bedSpawn = bedSpawn;
        this.anchorSpawn = anchorSpawn;
    }

    public Location getRespawnLocation() {
        return respawnLocation;
    }

    public void setRespawnLocation(Location respawnLocation) {
        Objects.requireNonNull(respawnLocation, "Respawn location can not be null");
        Objects.requireNonNull(respawnLocation.getWorld(), "Respawn world can not be null");
        this.respawnLocation = respawnLocation;
    }

    public boolean isBedSpawn() {
        return bedSpawn;
    }

    public boolean isAnchorSpawn() {
        return anchorSpawn;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

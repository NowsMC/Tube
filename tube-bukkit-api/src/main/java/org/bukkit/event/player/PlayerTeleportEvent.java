package org.bukkit.event.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerTeleportEvent extends PlayerMoveEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final TeleportCause cause;

    public PlayerTeleportEvent(Player player, Location from, Location to) {
        this(player, from, to, TeleportCause.PLUGIN);
    }

    public PlayerTeleportEvent(Player player, Location from, Location to, TeleportCause cause) {
        super(player, from, to);
        this.cause = cause;
    }

    public TeleportCause getCause() {
        return cause;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum TeleportCause {
        ENDER_PEARL,
        COMMAND,
        PLUGIN,
        NETHER_PORTAL,
        END_PORTAL,
        SPECTATE,
        END_GATEWAY,
        CHORUS_FRUIT,
        DISMOUNT,
        EXIT_BED,
        UNKNOWN
    }
}

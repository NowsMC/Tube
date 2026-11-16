package org.bukkit.event.player;

public class PlayerSpawnChangeEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.event.player.PlayerSpawnChangeEvent.Cause cause;
    private org.bukkit.Location newSpawn;
    private boolean forced;
    private boolean cancelled;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerSpawnChangeEvent(org.bukkit.entity.Player p0, org.bukkit.Location p1, boolean p2, org.bukkit.event.player.PlayerSpawnChangeEvent.Cause p3) {
        super(p0);
        this.newSpawn = p1;
        this.cause = p3;
        this.forced = p2;
    }

    public org.bukkit.event.player.PlayerSpawnChangeEvent.Cause getCause() {
        return cause;
    }

    public boolean isForced() {
        return forced;
    }

    public void setForced(boolean p0) {
        this.forced = p0;
    }

    public org.bukkit.Location getNewSpawn() {
        return newSpawn;
    }

    public void setNewSpawn(org.bukkit.Location p0) {
        throw new UnsupportedOperationException("PlayerSpawnChangeEvent#setNewSpawn is not supported by Tube yet");
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum Cause {
        COMMAND,
        BED,
        RESPAWN_ANCHOR,
        PLUGIN,
        RESET,
        UNKNOWN
    }
}

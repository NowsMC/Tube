package org.bukkit.event.world;

public class WorldUnloadEvent extends org.bukkit.event.world.WorldEvent implements org.bukkit.event.Cancellable {
    private boolean isCancelled;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public WorldUnloadEvent(org.bukkit.World p0) {
        super(p0);
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean p0) {
        this.isCancelled = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

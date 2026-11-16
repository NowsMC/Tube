package org.bukkit.event.hanging;

public class HangingBreakEvent extends org.bukkit.event.hanging.HangingEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.event.hanging.HangingBreakEvent.RemoveCause cause;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public HangingBreakEvent(org.bukkit.entity.Hanging p0, org.bukkit.event.hanging.HangingBreakEvent.RemoveCause p1) {
        super(p0);
        this.cause = p1;
    }

    public org.bukkit.event.hanging.HangingBreakEvent.RemoveCause getCause() {
        return cause;
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

    public enum RemoveCause {
        ENTITY,
        EXPLOSION,
        OBSTRUCTION,
        PHYSICS,
        DEFAULT
    }
}

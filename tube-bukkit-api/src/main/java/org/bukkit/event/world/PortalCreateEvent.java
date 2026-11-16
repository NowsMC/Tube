package org.bukkit.event.world;

public class PortalCreateEvent extends org.bukkit.event.world.WorldEvent implements org.bukkit.event.Cancellable {
    private boolean cancel;
    private java.util.List<org.bukkit.block.BlockState> blocks;
    private org.bukkit.entity.Entity entity;
    private org.bukkit.event.world.PortalCreateEvent.CreateReason reason;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PortalCreateEvent(java.util.List<org.bukkit.block.BlockState> p0, org.bukkit.World p1, org.bukkit.event.world.PortalCreateEvent.CreateReason p2) {
        this(p0, p1, (org.bukkit.entity.Entity) null, p2);
    }

    public PortalCreateEvent(java.util.List<org.bukkit.block.BlockState> p0, org.bukkit.World p1, org.bukkit.entity.Entity p2, org.bukkit.event.world.PortalCreateEvent.CreateReason p3) {
        super(p1);
        this.cancel = false;
        this.blocks = p0;
        this.entity = p2;
        this.reason = p3;
    }

    public java.util.List<org.bukkit.block.BlockState> getBlocks() {
        return blocks;
    }

    public org.bukkit.entity.Entity getEntity() {
        return entity;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    public org.bukkit.event.world.PortalCreateEvent.CreateReason getReason() {
        return reason;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum CreateReason {
        FIRE,
        NETHER_PAIR,
        END_PLATFORM
    }
}

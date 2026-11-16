package org.bukkit.event.world;

public class StructureGrowEvent extends org.bukkit.event.world.WorldEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.Location location;
    private org.bukkit.TreeType species;
    private boolean bonemeal;
    private org.bukkit.entity.Player player;
    private java.util.List<org.bukkit.block.BlockState> blocks;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public StructureGrowEvent(org.bukkit.Location p0, org.bukkit.TreeType p1, boolean p2, org.bukkit.entity.Player p3, java.util.List<org.bukkit.block.BlockState> p4) {
        super((org.bukkit.World) null);
        this.cancelled = false;
        this.location = p0;
        this.species = p1;
        this.bonemeal = p2;
        this.player = p3;
        this.blocks = p4;
    }

    public org.bukkit.Location getLocation() {
        return location;
    }

    public org.bukkit.TreeType getSpecies() {
        return species;
    }

    public boolean isFromBonemeal() {
        return bonemeal;
    }

    public org.bukkit.entity.Player getPlayer() {
        return player;
    }

    public java.util.List<org.bukkit.block.BlockState> getBlocks() {
        return blocks;
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
}

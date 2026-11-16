package org.bukkit.event.player;

public class PlayerTakeLecternBookEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    private boolean cancelled;
    private org.bukkit.block.Lectern lectern;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerTakeLecternBookEvent(org.bukkit.entity.Player p0, org.bukkit.block.Lectern p1) {
        super(p0);
        this.lectern = p1;
    }

    public org.bukkit.block.Lectern getLectern() {
        return lectern;
    }

    public org.bukkit.inventory.ItemStack getBook() {
        throw new UnsupportedOperationException("PlayerTakeLecternBookEvent#getBook is not supported by Tube yet");
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

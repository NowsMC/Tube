package org.bukkit.event.player;

public class PlayerItemHeldEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    private boolean cancel;
    private int previous;
    private int current;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerItemHeldEvent(org.bukkit.entity.Player p0, int p1, int p2) {
        super(p0);
        this.cancel = false;
        this.previous = p1;
        this.current = p2;
    }

    public int getPreviousSlot() {
        return previous;
    }

    public int getNewSlot() {
        return current;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean p0) {
        this.cancel = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

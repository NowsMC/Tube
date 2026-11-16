package org.bukkit.event.player;

public class PlayerChangedWorldEvent extends org.bukkit.event.player.PlayerEvent {
    private org.bukkit.World from;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerChangedWorldEvent(org.bukkit.entity.Player p0, org.bukkit.World p1) {
        super(p0);
        this.from = p1;
    }

    public org.bukkit.World getFrom() {
        return from;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

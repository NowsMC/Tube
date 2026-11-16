package org.bukkit.event.player;

public class PlayerCommandSendEvent extends org.bukkit.event.player.PlayerEvent {
    private java.util.Collection<java.lang.String> commands;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerCommandSendEvent(org.bukkit.entity.Player p0, java.util.Collection<java.lang.String> p1) {
        super(p0);
        this.commands = p1;
    }

    public java.util.Collection<java.lang.String> getCommands() {
        return commands;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

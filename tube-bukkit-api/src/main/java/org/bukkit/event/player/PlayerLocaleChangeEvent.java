package org.bukkit.event.player;

public class PlayerLocaleChangeEvent extends org.bukkit.event.player.PlayerEvent {
    private java.lang.String locale;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerLocaleChangeEvent(org.bukkit.entity.Player p0, java.lang.String p1) {
        super(p0);
        this.locale = p1;
    }

    public java.lang.String getLocale() {
        return locale;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

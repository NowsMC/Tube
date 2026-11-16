package org.bukkit.event.player;

public class PlayerAdvancementDoneEvent extends org.bukkit.event.player.PlayerEvent {
    private org.bukkit.advancement.Advancement advancement;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerAdvancementDoneEvent(org.bukkit.entity.Player p0, org.bukkit.advancement.Advancement p1) {
        super(p0);
        this.advancement = p1;
    }

    public org.bukkit.advancement.Advancement getAdvancement() {
        return advancement;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
